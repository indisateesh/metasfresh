package de.metas.product.impl;

import static org.adempiere.model.InterfaceWrapperHelper.loadOutOfTrx;

/*
 * #%L
 * de.metas.adempiere.adempiere.base
 * %%
 * Copyright (C) 2015 metas GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Properties;

import org.adempiere.ad.trx.api.ITrx;
import org.adempiere.mm.attributes.api.AttributeConstants;
import org.adempiere.model.InterfaceWrapperHelper;
import org.adempiere.service.IClientDAO;
import org.adempiere.uom.api.IUOMConversionBL;
import org.adempiere.uom.api.IUOMConversionContext;
import org.adempiere.uom.api.IUOMDAO;
import org.adempiere.util.Check;
import org.adempiere.util.Services;
import org.compiere.model.I_C_AcctSchema;
import org.compiere.model.I_C_UOM;
import org.compiere.model.I_M_AttributeSet;
import org.compiere.model.I_M_AttributeSetInstance;
import org.compiere.model.I_M_Product;
import org.compiere.model.I_M_Product_Category;
import org.compiere.model.I_M_Product_Category_Acct;
import org.compiere.model.MProductCategory;
import org.compiere.model.MProductCategoryAcct;
import org.compiere.model.X_M_Product;
import org.compiere.util.Env;
import org.slf4j.Logger;

import de.metas.costing.CostingLevel;
import de.metas.costing.CostingMethod;
import de.metas.logging.LogManager;
import de.metas.product.IProductBL;
import lombok.NonNull;

public final class ProductBL implements IProductBL
{
	private static final Logger logger = LogManager.getLogger(ProductBL.class);

	@Override
	public int getUOMPrecision(final I_M_Product product)
	{
		final Properties ctx = InterfaceWrapperHelper.getCtx(product);
		final int uomId = product.getC_UOM_ID();
		return Services.get(IUOMConversionBL.class).getPrecision(ctx, uomId);
	}

	@Override
	public String getMMPolicy(final I_M_Product product)
	{
		final MProductCategory pc = MProductCategory.get(Env.getCtx(), product.getM_Product_Category_ID());
		String policy = pc.getMMPolicy();
		if (policy == null || policy.length() == 0)
		{
			policy = Services.get(IClientDAO.class).retriveClient(Env.getCtx()).getMMPolicy();
		}
		return policy;
	}
	
	@Override
	public String getMMPolicy(final int productId)
	{
		Check.assume(productId > 0, "productId > 0");
		final I_M_Product product = loadOutOfTrx(productId, I_M_Product.class);
		return getMMPolicy(product);
	}

	@Override
	public I_C_UOM getStockingUOM(@NonNull final I_M_Product product)
	{
		return product.getC_UOM();
	}

	@Override
	public I_C_UOM getStockingUOM(final int productId)
	{
		Check.assume(productId > 0, "productId > 0");
		final I_M_Product product = InterfaceWrapperHelper.loadOutOfTrx(productId, I_M_Product.class);
		return getStockingUOM(product);
	}

	/**
	 *
	 * @param product
	 * @return UOM used for Product's Weight; never return null
	 */
	public I_C_UOM getWeightUOM(final I_M_Product product)
	{
		final Properties ctx = InterfaceWrapperHelper.getCtx(product);

		// FIXME: we hardcoded the UOM for M_Product.Weight to Kilogram
		return Services.get(IUOMDAO.class).retrieveByX12DE355(ctx, IUOMDAO.X12DE355_Kilogram);
	}

	@Override
	public BigDecimal getWeight(final I_M_Product product, final I_C_UOM uomTo)
	{
		Check.assumeNotNull(product, "product not null");
		Check.assumeNotNull(uomTo, "uomTo not null");

		final BigDecimal weightPerStockingUOM = product.getWeight();
		if (weightPerStockingUOM.signum() == 0)
		{
			return BigDecimal.ZERO;
		}

		final I_C_UOM stockingUom = getStockingUOM(product);

		//
		// Calculate the rate to convert from stocking UOM to "uomTo"
		final IUOMConversionBL uomConversionBL = Services.get(IUOMConversionBL.class);
		final IUOMConversionContext uomConversionCtx = uomConversionBL.createConversionContext(product);
		final BigDecimal stocking2uomToRate = uomConversionBL.convertQty(uomConversionCtx, BigDecimal.ONE, stockingUom, uomTo);

		//
		// Calculate the Weight for one "uomTo"
		final int weightPerUomToPrecision = getWeightUOM(product).getStdPrecision();
		final BigDecimal weightPerUomTo = weightPerStockingUOM
				.multiply(stocking2uomToRate)
				.setScale(weightPerUomToPrecision, RoundingMode.HALF_UP);

		return weightPerUomTo;
	}

	@Override
	public boolean isItem(final I_M_Product product)
	{
		if (X_M_Product.PRODUCTTYPE_Item.equals(product.getProductType()))
		{
			return true;
		}

// @formatter:off
// task 07246: IsDiverse does not automatically mean that the product is an item!
//		final de.metas.adempiere.model.I_M_Product productToUse = InterfaceWrapperHelper.create(product, de.metas.adempiere.model.I_M_Product.class);
//		if (productToUse.isDiverse())
//		{
//			return true;
//		}
// @formatter:on
		return false;
	}

	@Override
	public boolean isService(final I_M_Product product)
	{
		// i.e. PRODUCTTYPE_Service, PRODUCTTYPE_Resource, PRODUCTTYPE_Online
		return !isItem(product);
	}

	@Override
	public boolean isStocked(final I_M_Product product)
	{
		Check.assumeNotNull(product, "product not null");

		if (!product.isStocked())
		{
			return false;
		}

		return isItem(product);
	}

	@Override
	public boolean isStocked(final int productId)
	{
		Check.assume(productId > 0, "productId > 0");
		final I_M_Product product = loadOutOfTrx(productId, I_M_Product.class);
		return isStocked(product);
	}

	@Override
	public int getM_AttributeSet_ID(final I_M_Product product)
	{
		int attributeSet_ID = product.getM_AttributeSet_ID();

		if (attributeSet_ID > 0)
		{
			return attributeSet_ID;
		}
		if (product.getM_Product_Category_ID() <= 0) // guard against NPE which might happen in unit tests
		{
			return AttributeConstants.M_AttributeSet_ID_None;
		}

		attributeSet_ID = product.getM_Product_Category().getM_AttributeSet_ID();
		if (attributeSet_ID <= 0)
		{
			return AttributeConstants.M_AttributeSet_ID_None;
		}
		return attributeSet_ID;
	}

	@Override
	public int getM_AttributeSet_ID(final Properties ctx, final int productId)
	{
		Check.assume(productId > 0, "productId > 0");
		final I_M_Product product = InterfaceWrapperHelper.create(ctx, productId, I_M_Product.class, ITrx.TRXNAME_None);
		return getM_AttributeSet_ID(product);
	}

	@Override
	public I_M_AttributeSet getM_AttributeSet(final I_M_Product product)
	{
		if (product.getM_AttributeSet_ID() > 0)
		{
			return product.getM_AttributeSet();
		}

		final I_M_Product_Category productCategory = product.getM_Product_Category();
		if (productCategory.getM_AttributeSet_ID() > 0)
		{
			return productCategory.getM_AttributeSet();
		}

		return null;
	}

	@Override
	public I_M_AttributeSetInstance getCreateASI(final Properties ctx, final int M_AttributeSetInstance_ID, final int M_Product_ID)
	{
		// Load Instance if not 0
		if (M_AttributeSetInstance_ID > 0)
		{
			logger.debug("From M_AttributeSetInstance_ID={}", M_AttributeSetInstance_ID);
			return InterfaceWrapperHelper.create(ctx, M_AttributeSetInstance_ID, I_M_AttributeSetInstance.class, ITrx.TRXNAME_None);
		}

		// Get new from Product
		logger.debug("From M_Product_ID={}", M_Product_ID);
		if (M_Product_ID <= 0)
		{
			return null;
		}

		final I_M_Product product = InterfaceWrapperHelper.create(ctx, M_Product_ID, I_M_Product.class, ITrx.TRXNAME_None);

		final int attributeSetId = getM_AttributeSet_ID(product);
		final I_M_AttributeSetInstance asi = InterfaceWrapperHelper.create(ctx, I_M_AttributeSetInstance.class, ITrx.TRXNAME_None);
		asi.setM_AttributeSet_ID(attributeSetId);
		return asi;
	}	// get

	@Override
	public CostingLevel getCostingLevel(final I_M_Product product, final I_C_AcctSchema as)
	{
		final Properties ctx = InterfaceWrapperHelper.getCtx(product);
		final String trxName = InterfaceWrapperHelper.getTrxName(product);
		final I_M_Product_Category_Acct pca = MProductCategoryAcct.get(ctx, product.getM_Product_Category_ID(), as.getC_AcctSchema_ID(), trxName);

		// 07393
		// pca may be null. In this case, we take the costing level from the accounting schema

		String costingLevel = null;

		if (pca != null)
		{
			costingLevel = pca.getCostingLevel();
		}

		if (costingLevel == null)
		{
			costingLevel = as.getCostingLevel();
		}

		return CostingLevel.forCode(costingLevel);
	}

	@Override
	public CostingLevel getCostingLevel(final int productId, final I_C_AcctSchema as)
	{
		final I_M_Product product = loadOutOfTrx(productId, I_M_Product.class);
		return getCostingLevel(product, as);
	}

	@Override
	public CostingMethod getCostingMethod(final I_M_Product product, final I_C_AcctSchema as)
	{
		final Properties ctx = InterfaceWrapperHelper.getCtx(product);
		final String trxName = InterfaceWrapperHelper.getTrxName(product);
		final I_M_Product_Category_Acct pca = MProductCategoryAcct.get(ctx, product.getM_Product_Category_ID(), as.getC_AcctSchema_ID(), trxName);

		String costingMethod = null;

		if (pca != null)
		{
			costingMethod = pca.getCostingMethod();
		}
		if (costingMethod == null)
		{
			costingMethod = as.getCostingMethod();
		}
		return CostingMethod.ofCode(costingMethod);
	}

	@Override
	public CostingMethod getCostingMethod(final int productId, final I_C_AcctSchema as)
	{
		final I_M_Product product = loadOutOfTrx(productId, I_M_Product.class);
		return getCostingMethod(product, as);
	}

	@Override
	public boolean isTradingProduct(final I_M_Product product)
	{
		Check.assumeNotNull(product, "product not null");
		return product.isPurchased()
				&& product.isSold();
	}
}
