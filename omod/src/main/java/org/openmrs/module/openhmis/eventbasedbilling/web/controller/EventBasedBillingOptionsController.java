/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.openhmis.eventbasedbilling.web.controller;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.openhmis.cashier.api.ICashPointService;
import org.openmrs.module.openhmis.cashier.api.model.CashPoint;
import org.openmrs.module.openhmis.eventbasedbilling.api.IBillAssociatorDataService;
import org.openmrs.module.openhmis.eventbasedbilling.api.IEventBasedBillingOptionsService;
import org.openmrs.module.openhmis.eventbasedbilling.api.model.EventBasedBillingOptions;
import org.openmrs.module.openhmis.eventbasedbilling.api.model.IBillAssociator;
import org.openmrs.module.openhmis.eventbasedbilling.web.EventBasedBillingWebConstants;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
@RequestMapping(value = EventBasedBillingWebConstants.OPTIONS_PAGE)
public class  EventBasedBillingOptionsController {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	@RequestMapping(method = RequestMethod.GET)
	public void options(ModelMap model) {
		model.addAttribute("options", Context.getService(IEventBasedBillingOptionsService.class).getOptions());
		model.addAttribute("associators", Context.getService(IBillAssociatorDataService.class).getAll());
		model.addAttribute("cashPoints", Context.getService(ICashPointService.class).getAll());
	}

	@RequestMapping(method = RequestMethod.POST)
	public String save_options(@RequestParam(value = "isEnabled", required=false) String isEnabled,
            @RequestParam(value = "associatorId", required=false) Integer associatorId,
            @RequestParam(value = "cashPointId", required=false) Integer cashPointId) {
		IBillAssociator associator = null;
		if (associatorId != null) {
			try {
				associator = Context.getService(IBillAssociatorDataService.class).getById(associatorId);
			}
			catch (Exception e) {
				log.error("Failed to load bill associator based on form parameters.");
			}
		}

		CashPoint cashPoint = null;
		if (cashPointId != null) {
			try {
				cashPoint = Context.getService(ICashPointService.class).getById(cashPointId);
			}
			catch (Exception e) {
				log.error("Failed to load cash point based on form parameters.");
			}
		}

		EventBasedBillingOptions options = new EventBasedBillingOptions();
		options.setBillAssociator(associator);
		options.setCashPoint(cashPoint);
		options.setIsEnabled(!StringUtils.isEmpty(isEnabled));
		Context.getService(IEventBasedBillingOptionsService.class).saveOptions(options);
		
		return "redirect:" + EventBasedBillingWebConstants.OPTIONS_PAGE + ".form";
	}

}
