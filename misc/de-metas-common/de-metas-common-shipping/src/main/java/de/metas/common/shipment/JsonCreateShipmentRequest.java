/*
 * #%L
 * de-metas-common-shipmentschedule
 * %%
 * Copyright (C) 2020 metas GmbH
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

package de.metas.common.shipment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.common.collect.ImmutableList;
import de.metas.common.MeasurableRequest;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(builder = JsonCreateShipmentRequest.JsonCreateShipmentRequestBuilder.class)
public class JsonCreateShipmentRequest extends MeasurableRequest
{
	@JsonProperty("shipperCode")
	String shipperCode;

	@NonNull
	@JsonProperty("shipmentList")
	List<JsonCreateShipmentInfo> createShipmentInfoList;

	@Builder
	public JsonCreateShipmentRequest(
			@JsonProperty("shipperCode") final String shipperCode,
			@JsonProperty("shipmentList") final List<JsonCreateShipmentInfo> createShipmentInfoList)
	{
		this.shipperCode = shipperCode;
		this.createShipmentInfoList = createShipmentInfoList != null
				? createShipmentInfoList.stream().filter(Objects::nonNull).collect(Collectors.toList())
				: ImmutableList.of();
	}

	@Override
	@JsonIgnore
	public int getSize()
	{
		return createShipmentInfoList.size();
	}


	@JsonPOJOBuilder(withPrefix = "")
	public static class JsonCreateShipmentRequestBuilder
	{
	}
}
