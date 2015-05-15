package com.smartbear.msazuresupport;

import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.plugins.ActionConfiguration;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.smartbear.ActionGroups;
import com.smartbear.msazuresupport.utils.ApiImporter;
import com.smartbear.msazuresupport.utils.ApiListLoader;
import com.smartbear.msazuresupport.utils.ApiSelectorDialog;
import com.smartbear.msazuresupport.utils.AzureApi;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@ActionConfiguration(actionGroup = ActionGroups.OPEN_PROJECT_ACTIONS, separatorBefore = true)
public class AddAPIAction extends AbstractSoapUIAction<WsdlProject> {
    public AddAPIAction() {
        super("Add API From MS Azure", "Adds API from the MS Azure developer portal.");
    }

    @Override
    public void perform(WsdlProject project, Object o) {
        AzureApiInfo info = getAvailableApiList();
        List<AzureApi.ApiInfo> selectedAPIs = null;
        try (ApiSelectorDialog dlg = new ApiSelectorDialog(info.apis)) {
            selectedAPIs = dlg.getSelectedApi();
        }

        if (selectedAPIs != null) {
            List<RestService> services = ApiImporter.importServices(info.portalUrl.toString(), selectedAPIs, project);
            if (services.size() > 0) {
                UISupport.select(services.get(0));
            }
        }
    }

    private static class AzureApiInfo {
        public final URL portalUrl;
        public final List<AzureApi.ApiInfo> apis;

        public AzureApiInfo(URL portalUrl, List<AzureApi.ApiInfo> apis)  {
            this.portalUrl = portalUrl;
            this.apis = new ArrayList<>(apis);
        }
    }

    private AzureApiInfo getAvailableApiList() {
        String urlString = null;
        while (true) {
            urlString = UISupport.getDialogs().prompt("Input developer portal URL (i.e. developer.management.azure-api.net)", "Add API Specification from MS Azure", urlString);
            if (urlString == null) {
                UISupport.showErrorMessage("Empty URL");
                continue;
            }

            URL portalUrl = AzureApi.stringToUrl(urlString);
            if (portalUrl == null) {
                UISupport.showErrorMessage("Invalid URL");
                continue;
            }

            ApiListLoader.Result loaderResult = ApiListLoader.downloadList(portalUrl);
            if (loaderResult.canceled) {
                return null;
            }

            if (StringUtils.hasContent(loaderResult.error)) {
                UISupport.showErrorMessage(loaderResult.error);
                continue;
            }

            if (loaderResult.apis != null) {
                return new AzureApiInfo(portalUrl, loaderResult.apis);
            }
        }
    }
}