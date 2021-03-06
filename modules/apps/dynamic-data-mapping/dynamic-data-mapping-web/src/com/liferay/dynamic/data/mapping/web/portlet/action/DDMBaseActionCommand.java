/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.dynamic.data.mapping.web.portlet.action;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseActionCommand;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.util.HttpUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.model.Layout;
import com.liferay.portal.security.auth.PrincipalException;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portal.util.WebKeys;
import com.liferay.portlet.PortletPreferencesFactoryUtil;
import com.liferay.portlet.PortletURLImpl;
import com.liferay.portlet.StrictPortletPreferencesImpl;
import com.liferay.portlet.dynamicdatamapping.model.DDMStructure;
import com.liferay.portlet.dynamicdatamapping.model.DDMTemplate;
import com.liferay.portlet.dynamicdatamapping.model.DDMTemplateConstants;

import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;

/**
 * @author Leonardo Barros
 */
public abstract class DDMBaseActionCommand extends BaseActionCommand {

	protected String getRedirect(PortletRequest portletRequest) {
		String redirect = ParamUtil.getString(portletRequest, "redirect");

		String closeRedirect = ParamUtil.getString(
			portletRequest, "closeRedirect");

		if (Validator.isNull(closeRedirect)) {
			return redirect;
		}

		redirect = HttpUtil.setParameter(
			redirect, "closeRedirect", closeRedirect);

		SessionMessages.add(
			portletRequest,
			PortalUtil.getPortletId(portletRequest) +
				SessionMessages.KEY_SUFFIX_CLOSE_REDIRECT,
			closeRedirect);

		return redirect;
	}

	protected String getSaveAndContinueRedirect(
			PortletRequest portletRequest, DDMStructure structure,
			String redirect)
		throws Exception {

		ThemeDisplay themeDisplay = (ThemeDisplay)portletRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		String availableFields = ParamUtil.getString(
			portletRequest, "availableFields");
		String eventName = ParamUtil.getString(portletRequest, "eventName");

		PortletURLImpl portletURL = new PortletURLImpl(
			portletRequest, themeDisplay.getPpid(), themeDisplay.getPlid(),
			PortletRequest.RENDER_PHASE);

		portletURL.setParameter("mvcPath", "/edit_structure.jsp");
		portletURL.setParameter("redirect", redirect, false);
		portletURL.setParameter(
			"groupId", String.valueOf(structure.getGroupId()), false);

		long classNameId = PortalUtil.getClassNameId(DDMStructure.class);

		portletURL.setParameter(
			"classNameId", String.valueOf(classNameId), false);

		portletURL.setParameter(
			"classPK", String.valueOf(structure.getStructureId()), false);
		portletURL.setParameter("availableFields", availableFields, false);
		portletURL.setParameter("eventName", eventName, false);
		portletURL.setWindowState(portletRequest.getWindowState());

		return portletURL.toString();
	}

	protected String getSaveAndContinueRedirect(
			PortletRequest portletRequest, DDMTemplate template,
			String redirect)
		throws Exception {

		ThemeDisplay themeDisplay = (ThemeDisplay)portletRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		String portletResourceNamespace = ParamUtil.getString(
			portletRequest, "portletResourceNamespace");
		long classNameId = ParamUtil.getLong(portletRequest, "classNameId");
		long classPK = ParamUtil.getLong(portletRequest, "classPK");
		String structureAvailableFields = ParamUtil.getString(
			portletRequest, "structureAvailableFields");

		PortletURLImpl portletURL = new PortletURLImpl(
			portletRequest, themeDisplay.getPpid(), themeDisplay.getPlid(),
			PortletRequest.RENDER_PHASE);

		portletURL.setParameter("mvcPath", "/edit_template.jsp");
		portletURL.setParameter("redirect", redirect, false);
		portletURL.setParameter(
			"portletResourceNamespace", portletResourceNamespace, false);
		portletURL.setParameter(
			"templateId", String.valueOf(template.getTemplateId()), false);
		portletURL.setParameter(
			"groupId", String.valueOf(template.getGroupId()), false);
		portletURL.setParameter(
			"classNameId", String.valueOf(classNameId), false);
		portletURL.setParameter("classPK", String.valueOf(classPK), false);
		portletURL.setParameter("type", template.getType(), false);
		portletURL.setParameter(
			"structureAvailableFields", structureAvailableFields, false);
		portletURL.setWindowState(portletRequest.getWindowState());

		return portletURL.toString();
	}

	protected PortletPreferences getStrictPortletSetup(
			Layout layout, String portletId)
		throws PortalException {

		if (Validator.isNull(portletId)) {
			return null;
		}

		PortletPreferences portletPreferences =
			PortletPreferencesFactoryUtil.getStrictPortletSetup(
				layout, portletId);

		if (portletPreferences instanceof StrictPortletPreferencesImpl) {
			throw new PrincipalException();
		}

		return portletPreferences;
	}

	protected PortletPreferences getStrictPortletSetup(
			PortletRequest portletRequest)
		throws PortalException {

		String portletResource = ParamUtil.getString(
			portletRequest, "portletResource");

		ThemeDisplay themeDisplay = (ThemeDisplay)portletRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		return getStrictPortletSetup(themeDisplay.getLayout(), portletResource);
	}

	protected void setRedirectAttribute(PortletRequest portletRequest) {
		String redirect = getRedirect(portletRequest);

		portletRequest.setAttribute(WebKeys.REDIRECT, redirect);
	}

	protected void setRedirectAttribute(
			PortletRequest portletRequest, DDMStructure structure)
		throws Exception {

		String redirect = getRedirect(portletRequest);

		boolean saveAndContinue = ParamUtil.getBoolean(
			portletRequest, "saveAndContinue");

		if (saveAndContinue) {
			redirect = getSaveAndContinueRedirect(
				portletRequest, structure, redirect);
		}

		portletRequest.setAttribute(WebKeys.REDIRECT, redirect);
	}

	protected void setRedirectAttribute(
			PortletRequest portletRequest, DDMTemplate template)
		throws Exception {

		String redirect = getRedirect(portletRequest);

		boolean saveAndContinue = ParamUtil.getBoolean(
			portletRequest, "saveAndContinue");

		if (saveAndContinue) {
			redirect = getSaveAndContinueRedirect(
				portletRequest, template, redirect);
		}

		portletRequest.setAttribute(WebKeys.REDIRECT, redirect);
	}

	protected void updatePortletPreferences(
			PortletRequest portletRequest, DDMTemplate template)
		throws Exception {

		PortletPreferences portletPreferences = getStrictPortletSetup(
			portletRequest);

		if (portletPreferences == null) {
			return;
		}

		String templateType = template.getType();

		if (templateType.equals(DDMTemplateConstants.TEMPLATE_TYPE_DISPLAY)) {
			portletPreferences.setValue(
				"displayDDMTemplateId",
				String.valueOf(template.getTemplateId()));
		}
		else {
			portletPreferences.setValue(
				"formDDMTemplateId", String.valueOf(template.getTemplateId()));
		}

		portletPreferences.store();
	}

}