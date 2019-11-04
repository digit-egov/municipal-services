package org.egov.waterConnection.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.egov.waterConnection.model.Property;
import org.egov.waterConnection.model.PropertyCriteria;
import org.egov.waterConnection.model.PropertyRequest;
import org.egov.waterConnection.model.PropertyResponse;
import org.egov.waterConnection.model.RequestInfoWrapper;
import org.egov.waterConnection.model.SewerageConnectionRequest;
import org.egov.waterConnection.model.WaterConnectionRequest;
import org.egov.waterConnection.repository.ServiceRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.databind.ObjectMapper;

public class SewerageServicesUtil {

	private ServiceRequestRepository serviceRequestRepository;

	@Value("${egov.property.service.host}")
	private String propertyHost;

	@Value("${egov.property.createendpoint}")
	private String createPropertyEndPoint;

	@Value("${egov.property.searchendpoint}")
	private String searchPropertyEndPoint;

	@Autowired
	public SewerageServicesUtil(ServiceRequestRepository serviceRequestRepository) {
		this.serviceRequestRepository = serviceRequestRepository;

	}

	/**
	 * 
	 * @param waterConnectionRequest
	 *            WaterConnectionRequest containing property
	 * @return List of Property
	 */
	public List<Property> propertySearch(SewerageConnectionRequest sewerageConnectionRequest) {
		Set<String> propertyIds = new HashSet<>();
		List<Property> propertyList = new ArrayList<>();
		PropertyCriteria propertyCriteria = new PropertyCriteria();
		HashMap<String, Object> propertyRequestObj = new HashMap<>();
		propertyIds.add(sewerageConnectionRequest.getSewerageConnection().getId());
		propertyCriteria.setIds(propertyIds);
		propertyRequestObj.put("RequestInfoWrapper", getPropertyRequestInfoWrapperSearch(new RequestInfoWrapper(),
				sewerageConnectionRequest.getRequestInfo()));
		propertyRequestObj.put("PropertyCriteria", propertyCriteria);
		Object result = serviceRequestRepository.fetchResult(getPropertySearchURL(), propertyRequestObj);
		propertyList = getPropertyDetails(result);
		if (propertyList == null || propertyList.isEmpty()) {
			throw new CustomException("INCORRECT PROPERTY ID", "SEWERAGE CONNECTION CAN NOT BE CREATE");
		}
		return propertyList;
	}

	private RequestInfoWrapper getPropertyRequestInfoWrapperSearch(RequestInfoWrapper requestInfoWrapper,
			RequestInfo requestInfo) {
		RequestInfoWrapper requestInfoWrapper_new = RequestInfoWrapper.builder().requestInfo(requestInfo).build();
		return requestInfoWrapper_new;
	}

	public StringBuilder getPropertySearchURL() {
		return new StringBuilder().append(propertyHost).append(searchPropertyEndPoint);
	}

	/**
	 * 
	 * @param result
	 *            Response object from property service call
	 * @return List of property
	 */
	private List<Property> getPropertyDetails(Object result) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			PropertyResponse propertyResponse = mapper.convertValue(result, PropertyResponse.class);
			return propertyResponse.getProperties();
		} catch (Exception ex) {
			throw new CustomException("PARSING ERROR", "The property json cannot be parsed");
		}
	}

	/**
	 * 
	 * @param waterConnectionRequest
	 * @return Created property list
	 */
	public List<Property> createPropertyRequest(SewerageConnectionRequest sewerageConnectionRequest) {
		List<Property> propertyList = new ArrayList<>();
		propertyList.add(sewerageConnectionRequest.getSewerageConnection().getProperty());
		PropertyRequest propertyReq = getPropertyRequest(sewerageConnectionRequest.getRequestInfo(), propertyList);
		Object result = serviceRequestRepository.fetchResult(getPropertyCreateURL(), propertyReq);
		return getPropertyDetails(result);
	}

	public StringBuilder getPropertyCreateURL() {
		return new StringBuilder().append(propertyHost).append(createPropertyEndPoint);
	}

	private PropertyRequest getPropertyRequest(RequestInfo requestInfo, List<Property> propertyList) {
		PropertyRequest propertyReq = PropertyRequest.builder().requestInfo(requestInfo).properties(propertyList)
				.build();
		return propertyReq;
	}

}
