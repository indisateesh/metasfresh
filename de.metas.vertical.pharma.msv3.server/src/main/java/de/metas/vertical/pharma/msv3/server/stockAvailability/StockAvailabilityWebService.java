package de.metas.vertical.pharma.msv3.server.stockAvailability;

import javax.xml.bind.JAXBElement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import de.metas.vertical.pharma.msv3.protocol.stockAvailability.StockAvailabilityJAXBConverters;
import de.metas.vertical.pharma.msv3.protocol.stockAvailability.StockAvailabilityQuery;
import de.metas.vertical.pharma.msv3.protocol.stockAvailability.StockAvailabilityResponse;
import de.metas.vertical.pharma.vendor.gateway.msv3.schema.VerfuegbarkeitAnfragen;
import de.metas.vertical.pharma.vendor.gateway.msv3.schema.VerfuegbarkeitAnfragenResponse;

/*
 * #%L
 * metasfresh-pharma.msv3.server
 * %%
 * Copyright (C) 2018 metas GmbH
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

@Endpoint
public class StockAvailabilityWebService
{
	public static final String WSDL_BEAN_NAME = "Msv3VerfuegbarkeitAnfragenService";

	@Autowired
	private StockAvailabilityService stockAvailabilityService;
	@Autowired
	private StockAvailabilityJAXBConverters jaxbConverters;

	@PayloadRoot(localPart = "verfuegbarkeitAnfragen", namespace = "urn:msv3:v2")
	public @ResponsePayload JAXBElement<VerfuegbarkeitAnfragenResponse> verfuegbarkeitAnfragen(@RequestPayload final JAXBElement<VerfuegbarkeitAnfragen> jaxbRequest)
	{
		final VerfuegbarkeitAnfragen soapRequest = jaxbRequest.getValue();
		assertValidClientSoftwareId(soapRequest.getClientSoftwareKennung());

		final StockAvailabilityQuery stockAvailabilityQuery = jaxbConverters.fromJAXB(soapRequest.getVerfuegbarkeitsanfrage());
		final StockAvailabilityResponse stockAvailabilityResponse = stockAvailabilityService.checkAvailability(stockAvailabilityQuery);

		return jaxbConverters.toJAXB(stockAvailabilityResponse);
	}

	private void assertValidClientSoftwareId(final String clientSoftwareId)
	{
		// TODO
	}
}
