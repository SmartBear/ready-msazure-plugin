package com.smartbear.msazuresupport.utils;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.x.dialogs.Worker;
import com.eviware.x.dialogs.XProgressDialog;
import com.eviware.x.dialogs.XProgressMonitor;
import com.smartbear.msazuresupport.Strings;
import com.smartbear.msazuresupport.entities.ApiInfo;

import java.util.List;

public class ApiListLoader implements Worker {

    public static class Result {
        public List<ApiInfo> apis = null;
        public String error = null;
        public boolean authorizationFailed = false;
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

    private AzureApi.ConnectionSettings connectionSettings;
    private XProgressDialog waitDialog;
    private String apiRetrievingError = null;

    Result result = new Result();

    private ApiListLoader(AzureApi.ConnectionSettings connectionSettings, XProgressDialog waitDialog) {
        this.connectionSettings = connectionSettings;
        this.waitDialog = waitDialog;
    }

    public static Result downloadList(AzureApi.ConnectionSettings connection) {
        ApiListLoader worker = new ApiListLoader(connection, UISupport.getDialogs().createProgressDialog(Strings.Executing.QUERY_API_PROGRESS, 0, "", true));
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
            result.authorizationFailed = false;
            result.apis = AzureApi.getApiList(connectionSettings);
        } catch (InvalidAuthorizationException e) {
            result.authorizationFailed = true;
            handleError(e);
        } catch (Throwable e) {
            handleError(e);
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
            result.addError(Strings.Executing.QUERY_API_ERROR + apiRetrievingError);
            return;
        }
        if (result.apis == null || result.apis.size() == 0) {
            result.addError(Strings.Executing.QUERY_API_EMPTY_ERROR);
        }
    }

    @Override
    public boolean onCancel() {
        result.cancel();
        waitDialog.setVisible(false);
        return true;
    }

    private void handleError(Throwable e) {
        SoapUI.logError(e);
        apiRetrievingError = e.getMessage();
        if (StringUtils.isNullOrEmpty(apiRetrievingError)) {
            apiRetrievingError = e.getClass().getName();
        }
    }
}
