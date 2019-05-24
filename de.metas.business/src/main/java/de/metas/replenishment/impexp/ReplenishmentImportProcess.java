package de.metas.replenishment.impexp;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import org.adempiere.ad.trx.api.ITrx;
import org.adempiere.impexp.AbstractImportProcess;
import org.adempiere.model.InterfaceWrapperHelper;
import org.adempiere.util.lang.IMutable;
import org.compiere.model.I_I_Replenish;
import org.compiere.model.I_M_Replenish;
import org.compiere.model.X_I_Replenish;

import lombok.NonNull;

/*
 * #%L
 * de.metas.business
 * %%
 * Copyright (C) 2019 metas GmbH
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

public class ReplenishmentImportProcess extends AbstractImportProcess<I_I_Replenish>
{

	@Override
	public Class<I_I_Replenish> getImportModelClass()
	{
		return I_I_Replenish.class;
	}

	@Override
	public String getImportTableName()
	{
		return I_I_Replenish.Table_Name;
	}

	@Override
	protected String getTargetTableName()
	{
		return I_M_Replenish.Table_Name;
	}

	@Override
	protected void updateAndValidateImportRecords()
	{
		RepelnishmentImportTableSqlUpdater.updateReplenishmentImortTable(getWhereClause());
	}

	@Override
	protected String getImportOrderBySql()
	{
		return I_I_Replenish.COLUMNNAME_ProductValue;
	}

	@Override
	protected I_I_Replenish retrieveImportRecord(Properties ctx, ResultSet rs) throws SQLException
	{
		return new X_I_Replenish(ctx, rs, ITrx.TRXNAME_ThreadInherited);
	}

	/*
	 * @param isInsertOnly ignored. This import is only for updates.
	 */
	@Override
	protected ImportRecordResult importRecord(@NonNull IMutable<Object> state,
			@NonNull final I_I_Replenish importRecord,
			final boolean isInsertOnly)
	{
		if (ReplenishImportHelper.isValidRecordForImport(importRecord))
		{

			return importReplenish(importRecord);
		}
		else
		{
			return ImportRecordResult.Nothing; 
		}

	}

	private ImportRecordResult importReplenish(@NonNull final I_I_Replenish importRecord)
	{
		final ImportRecordResult replenishImportResult;

		final I_M_Replenish replenish;
		if (importRecord.getM_Replenish_ID() <= 0)
		{
			replenish = ReplenishImportHelper.createNewReplenish(importRecord);
			replenishImportResult = ImportRecordResult.Inserted;
		}
		else
		{
			replenish = ReplenishImportHelper.uppdateReplenish(importRecord);
			replenishImportResult = ImportRecordResult.Updated;
		}

		InterfaceWrapperHelper.save(replenish);

		importRecord.setM_Replenish_ID(replenish.getM_Replenish_ID());
		InterfaceWrapperHelper.save(importRecord);

		return replenishImportResult;
	}
	
	@Override
	protected void markImported(@NonNull final I_I_Replenish importRecord)
	{
		importRecord.setI_IsImported(X_I_Replenish.I_ISIMPORTED_Imported);
		importRecord.setProcessed(true);
		InterfaceWrapperHelper.save(importRecord);
	}
}
