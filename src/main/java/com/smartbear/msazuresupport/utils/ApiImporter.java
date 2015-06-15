package com.smartbear.msazuresupport.utils;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.support.UISupport;
import com.eviware.x.dialogs.Worker;
import com.eviware.x.dialogs.XProgressDialog;
import com.eviware.x.dialogs.XProgressMonitor;
import com.smartbear.msazuresupport.Strings;

import java.util.ArrayList;
import java.util.List;

public final class ApiImporter implements Worker {
    private Boolean canceled = false;
    private final XProgressDialog waitDialog;
    private final AzureApi.ConnectionSettings connectionSettings;
    private final List<AzureApi.ApiInfo> apis;
    private final WsdlProject project;
    private final ArrayList<RestService> addedServices = new ArrayList<>();
    private final StringBuilder errors = new StringBuilder();


    private ApiImporter(XProgressDialog waitDialog, AzureApi.ConnectionSettings connectionSettings, List<AzureApi.ApiInfo> apis, WsdlProject project) {
        this.waitDialog = waitDialog;
        this.connectionSettings = connectionSettings;
        this.apis = apis;
        this.project = project;
    }

    public static List<RestService> importServices(AzureApi.ConnectionSettings connectionSettings, List<AzureApi.ApiInfo> apis, WsdlProject project) {
        ApiImporter worker = new ApiImporter(UISupport.getDialogs().createProgressDialog(Strings.Executing.IMPORT_PROGRESS, 100, "", true), connectionSettings, apis, project);
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
                RestService service = AzureApi.importApiToProject(connectionSettings, api, project);
                addedServices.add(service);
            } catch (Throwable e) {
                SoapUI.logError(e);
                errors.append(String.format(Strings.Executing.IMPORT_ERROR, api.name, e.getMessage()));
            }
        }

        if (errors.length() > 0) {
            errors.append(Strings.Executing.IMPORT_ERROR_TAIL);
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
