package com.smartbear.msazuresupport;

import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.plugins.ActionConfiguration;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.smartbear.ActionGroups;
import com.smartbear.msazuresupport.entities.ApiInfo;
import com.smartbear.msazuresupport.entities.Subscription;
import com.smartbear.msazuresupport.utils.AddApiDialog;
import com.smartbear.msazuresupport.utils.ApiImporter;
import com.smartbear.msazuresupport.utils.ApiListLoader;
import com.smartbear.msazuresupport.utils.ApiSelectorDialog;
import com.smartbear.msazuresupport.utils.AzureApi;
import com.smartbear.msazuresupport.utils.SubscriptionKeyInputDialog;
import com.smartbear.msazuresupport.utils.SubscriptionsLoader;
import com.smartbear.rapisupport.ServiceFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@ActionConfiguration(actionGroup = ActionGroups.OPEN_PROJECT_ACTIONS, separatorBefore = true)
public class AddAPIAction extends AbstractSoapUIAction<WsdlProject> {
    public AddAPIAction() {
        super(Strings.AddApiAction.NAME, Strings.AddApiAction.DESCRIPTION);
    }

    @Override
    public void perform(WsdlProject project, Object o) {
        AzureApiInfo info = getAvailableApiList();
        if (info == null) {
            return;
        }

        ApiSelectorDialog.Result selResult = null;
        try (ApiSelectorDialog dlg = new ApiSelectorDialog(info.apis)) {
            selResult = dlg.getSelectedApi();
        }

        if (selResult != null) {
            AzureApi.ConnectionSettings connectionSettings = new AzureApi.ConnectionSettings(info.portalUrl, info.accessToken);
            List<Subscription> subscriptions = SubscriptionsLoader.downloadSubscriptions(connectionSettings);

            try (SubscriptionKeyInputDialog dlg = new SubscriptionKeyInputDialog(selResult.selectedAPIs, subscriptions)) {
                if (!dlg.show()) {
                    return;
                }
            }

            List<RestService> services = ApiImporter.importServices(connectionSettings, selResult.selectedAPIs, project);
            ServiceFactory.Build(project, services, selResult.entities);
            if (services.size() > 0) {
                UISupport.select(services.get(0));
            }
        }
    }

    private static class AzureApiInfo {
        public final URL portalUrl;
        public final String accessToken;
        public final List<ApiInfo> apis;

        public AzureApiInfo(URL portalUrl, String accessToken, List<ApiInfo> apis) {
            this.portalUrl = portalUrl;
            this.accessToken = accessToken;
            this.apis = new ArrayList<>(apis);
        }
    }

    private AzureApiInfo getAvailableApiList() {
        while (true) {
            AddApiDialog.Result dialogResult = null;
            try (AddApiDialog dlg = new AddApiDialog()) {
                dialogResult = dlg.show();
            }
            if (dialogResult == null) {
                return null;
            }

            ApiListLoader.Result loaderResult = ApiListLoader.downloadList(new AzureApi.ConnectionSettings(dialogResult.portalUrl, dialogResult.accessToken));
            if (loaderResult.canceled) {
                return null;
            }

            if (StringUtils.hasContent(loaderResult.error)) {
                UISupport.showErrorMessage(loaderResult.error);
                continue;
            }

            if (loaderResult.apis != null) {
                return new AzureApiInfo(AzureApi.stringToUrl(dialogResult.portalUrl), dialogResult.accessToken, loaderResult.apis);
            }
        }
    }
}
