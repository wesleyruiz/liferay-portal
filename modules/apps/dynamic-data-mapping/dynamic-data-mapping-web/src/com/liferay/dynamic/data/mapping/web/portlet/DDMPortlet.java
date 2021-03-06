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

package com.liferay.dynamic.data.mapping.web.portlet;

import com.liferay.portal.LocaleException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.security.auth.PrincipalException;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portal.util.PortletKeys;
import com.liferay.portal.util.WebKeys;
import com.liferay.portlet.dynamicdatamapping.NoSuchStructureException;
import com.liferay.portlet.dynamicdatamapping.NoSuchTemplateException;
import com.liferay.portlet.dynamicdatamapping.RequiredStructureException;
import com.liferay.portlet.dynamicdatamapping.RequiredTemplateException;
import com.liferay.portlet.dynamicdatamapping.StructureDefinitionException;
import com.liferay.portlet.dynamicdatamapping.StructureDuplicateElementException;
import com.liferay.portlet.dynamicdatamapping.StructureNameException;
import com.liferay.portlet.dynamicdatamapping.TemplateNameException;
import com.liferay.portlet.dynamicdatamapping.TemplateScriptException;
import com.liferay.portlet.dynamicdatamapping.TemplateSmallImageNameException;
import com.liferay.portlet.dynamicdatamapping.TemplateSmallImageSizeException;
import com.liferay.portlet.dynamicdatamapping.model.DDMStructure;
import com.liferay.portlet.dynamicdatamapping.model.DDMTemplate;
import com.liferay.portlet.dynamicdatamapping.service.DDMStructureServiceUtil;
import com.liferay.portlet.dynamicdatamapping.service.DDMTemplateLocalServiceUtil;

import java.io.IOException;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.Portlet;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.osgi.service.component.annotations.Component;

/**
 * @author Leonardo Barros
 */
@Component(
	immediate = true,
	property = {
		"com.liferay.portlet.add-default-resource=true",
		"com.liferay.portlet.autopropagated-parameters=refererPortletName",
		"com.liferay.portlet.autopropagated-parameters=refererWebDAVToken",
		"com.liferay.portlet.autopropagated-parameters=scopeTitle",
		"com.liferay.portlet.autopropagated-parameters=showAncestorScopes",
		"com.liferay.portlet.autopropagated-parameters=showBackURL",
		"com.liferay.portlet.autopropagated-parameters=showManageTemplates",
		"com.liferay.portlet.autopropagated-parameters=showToolbar",
		"com.liferay.portlet.css-class-wrapper=portlet-dynamic-data-mapping",
		"com.liferay.portlet.display-category=category.hidden",
		"com.liferay.portlet.header-portlet-css=/css/main.css",
		"com.liferay.portlet.header-portlet-javascript=/js/main.js",
		"com.liferay.portlet.header-portlet-javascript=/js/custom_fields.js",
		"com.liferay.portlet.preferences-owned-by-group=true",
		"com.liferay.portlet.private-request-attributes=false",
		"com.liferay.portlet.private-session-attributes=false",
		"com.liferay.portlet.render-weight=50",
		"com.liferay.portlet.use-default-template=true",
		"javax.portlet.display-name=Dynamic Data Mapping Web",
		"javax.portlet.expiration-cache=0",
		"javax.portlet.init-param.template-path=/",
		"javax.portlet.init-param.view-template=/view.jsp",
		"javax.portlet.name="+ PortletKeys.DYNAMIC_DATA_MAPPING,
		"javax.portlet.resource-bundle=content.Language",
		"javax.portlet.security-role-ref=power-user,user",
		"javax.portlet.supports.mime-type=text/html"
	},
	service = Portlet.class
)
public class DDMPortlet extends MVCPortlet {

	@Override
	public void processAction(
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws IOException, PortletException {

		try {
			super.processAction(actionRequest, actionResponse);
		}
		catch (Exception e) {
			if (e instanceof NoSuchStructureException ||
				e instanceof PrincipalException ||
				e instanceof NoSuchTemplateException) {

				SessionErrors.add(actionRequest, e.getClass());

				include("/error.jsp", actionRequest, actionResponse);
			}
			else if (e instanceof LocaleException ||
					 e instanceof RequiredStructureException ||
					 e instanceof StructureDefinitionException ||
					 e instanceof StructureDuplicateElementException ||
					 e instanceof StructureNameException ||
					 e instanceof TemplateNameException ||
					 e instanceof RequiredTemplateException ||
					 e instanceof TemplateNameException ||
					 e instanceof TemplateScriptException ||
					 e instanceof TemplateSmallImageNameException ||
					 e instanceof TemplateSmallImageSizeException) {

				SessionErrors.add(actionRequest, e.getClass(), e);

				if (e instanceof RequiredStructureException ||
					e instanceof RequiredTemplateException) {

					String redirect = PortalUtil.escapeRedirect(
						ParamUtil.getString(actionRequest, "redirect"));

					if (Validator.isNotNull(redirect)) {
						actionResponse.sendRedirect(redirect);
					}
				}
			}
			else {
				throw e;
			}
		}
	}

	@Override
	public void render(RenderRequest request, RenderResponse response)
		throws IOException, PortletException {

		try {
			setDDMTemplateRequestAttribute(request);

			setDDMStructureRequestAttribute(request);
		}
		catch (NoSuchStructureException nsse) {

			// Let this slide because the user can manually input a structure
			// key for a new structure that does not yet exist

		}
		catch (NoSuchTemplateException nste) {

			// Let this slide because the user can manually input a template key
			// for a new template that does not yet exist

			if (_log.isDebugEnabled()) {
				_log.debug(nste, nste);
			}
		}
		catch (Exception e) {
			if (e instanceof PrincipalException) {
				SessionErrors.add(request, e.getClass());

				include("/error.jsp", request, response);
			}
			else {
				throw new PortletException(e);
			}
		}

		super.render(request, response);
	}

	protected void setDDMStructureRequestAttribute(RenderRequest renderRequest)
		throws PortalException {

		long classNameId = ParamUtil.getLong(renderRequest, "classNameId");
		long classPK = ParamUtil.getLong(renderRequest, "classPK");

		if ((classNameId > 0) && (classPK > 0)) {
			DDMStructure structure = null;

			long structureClassNameId = PortalUtil.getClassNameId(
				DDMStructure.class);

			if ((structureClassNameId == classNameId) && (classPK > 0)) {
				structure = DDMStructureServiceUtil.getStructure(classPK);
			}

			renderRequest.setAttribute(
				WebKeys.DYNAMIC_DATA_MAPPING_STRUCTURE, structure);
		}
	}

	protected void setDDMTemplateRequestAttribute(RenderRequest renderRequest)
		throws PortalException {

		long templateId = ParamUtil.getLong(renderRequest, "templateId");

		if (templateId > 0) {
			DDMTemplate template = DDMTemplateLocalServiceUtil.getDDMTemplate(
				templateId);

			renderRequest.setAttribute(
				WebKeys.DYNAMIC_DATA_MAPPING_TEMPLATE, template);
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(DDMPortlet.class);

}