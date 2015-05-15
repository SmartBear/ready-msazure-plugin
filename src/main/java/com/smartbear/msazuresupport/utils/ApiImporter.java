package com.smartbear.msazuresupport.utils;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.support.UISupport;
import com.eviware.x.dialogs.Worker;
import com.eviware.x.dialogs.XProgressDialog;
import com.eviware.x.dialogs.XProgressMonitor;

import java.util.ArrayList;
import java.util.List;

public final class ApiImporter implements Worker {
    private Boolean canceled = false;
    private final XProgressDialog waitDialog;
    private final String portalUrl;
    private final List<AzureApi.ApiInfo> apis;
    private final WsdlProject project;
    private final ArrayList<RestService> addedServices = new ArrayList<>();
    private final StringBuilder errors = new StringBuilder();


    private ApiImporter(XProgressDialog waitDialog, String portalUrl, List<AzureApi.ApiInfo> apis, WsdlProject project) {
        this.waitDialog = waitDialog;
        this.portalUrl = portalUrl;
        this.apis = apis;
        this.project = project;
    }

    public static List<RestService> importServices(String portalUrl, List<AzureApi.ApiInfo> apis, WsdlProject project) {
        ApiImporter worker = new ApiImporter(UISupport.getDialogs().createProgressDialog("Importing APIs...", 100, "", true), portalUrl, apis, project);
        try {
            worker.waitDialog.run(worker);
        } catch (Exception e) {
            UISupport.showErrorMessage(e.getMessage());
            SoapUI.logError(e);
        }

        return worker.addedServices;
    }

    @Override
    public Object construct(XProgressMonitor xProgressMonitor) {
        for (AzureApi.ApiInfo api : apis) {
            try {
                RestService service = AzureApi.importApiToProject(portalUrl, api, project);
                addedServices.add(service);
            } catch (Throwable e) {
                SoapUI.logError(e);
                errors.append(String.format("Failed to read API description for[%s] - [%s]\n", api.name, e.getMessage()));
            }
        }

        if (errors.length() > 0) {
            errors.append("Please contact MS Azure support for assistance");
        }
        return null;
    }

    @Override
    public void finished() {
        if (canceled) {
            return;
        }
        waitDialog.setVisible(false);
        if (errors.length() > 0) {
            UISupport.showErrorMessage(errors.toString());
        }
    }

    @Override
    public boolean onCancel() {
        canceled = true;
        waitDialog.setVisible(false);
        return true;
    }
}