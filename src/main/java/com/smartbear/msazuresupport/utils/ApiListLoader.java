package com.smartbear.msazuresupport.utils;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.x.dialogs.Worker;
import com.eviware.x.dialogs.XProgressDialog;
import com.eviware.x.dialogs.XProgressMonitor;

import java.net.URL;
import java.util.List;

public class ApiListLoader implements Worker {

    public static class Result {
        public List<AzureApi.ApiInfo> apis = null;
        public String error = null;
        public boolean canceled = false;

        public void addError(String errorText) {
            apis = null;
            error = error == null ? errorText : error + "\n" + errorText;
        }

        public void cancel() {
            canceled = true;
            apis = null;
        }
    }

    private URL url;
    private XProgressDialog waitDialog;
    private String apiRetrievingError = null;

    Result result = new Result();

    private ApiListLoader(URL apiPortalUrl, XProgressDialog waitDialog) {
        this.url = apiPortalUrl;
        this.waitDialog = waitDialog;
    }

    public static Result downloadList(URL developerPortalUrl) {
        ApiListLoader worker = new ApiListLoader(developerPortalUrl, UISupport.getDialogs().createProgressDialog("Getting APIs List...", 0, "", true));
        try {
            worker.waitDialog.run(worker);
        } catch (Exception ex) {
            SoapUI.logError(ex);
            worker.result.addError(ex.getMessage());
        }
        return worker.result;
    }

    @Override
    public Object construct(XProgressMonitor xProgressMonitor) {
        try {
            result.apis = AzureApi.getApiList(url);
        } catch (Throwable e) {
            SoapUI.logError(e);
            apiRetrievingError = e.getMessage();
            if (StringUtils.isNullOrEmpty(apiRetrievingError)) {
                apiRetrievingError = e.getClass().getName();
            }
        }
        return null;
    }

    @Override
    public void finished() {
        if (result.canceled) {
            return;
        }
        waitDialog.setVisible(false);
        if (StringUtils.hasContent(apiRetrievingError)) {
            result.addError("Unable to read API list from the specified MS Azure developer portal because of the following error:\n" + apiRetrievingError);
            return;
        }
        if (result.apis == null || result.apis.size() == 0) {
            result.addError("No API is accessible at the specified URL or registered correctly.");
        }
    }

    @Override
    public boolean onCancel() {
        result.cancel();
        waitDialog.setVisible(false);
        return true;
    }
}
