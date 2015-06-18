package com.smartbear.msazuresupport.utils;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.x.dialogs.Worker;
import com.eviware.x.dialogs.XProgressDialog;
import com.eviware.x.dialogs.XProgressMonitor;
import com.smartbear.msazuresupport.Strings;
import com.smartbear.msazuresupport.entities.Subscription;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SubscriptionsLoader implements Worker {
    private boolean canceled = false;
    private final XProgressDialog waitDialog;
    private final AzureApi.ConnectionSettings connectionSettings;
    private final List<Subscription> subscriptions = new ArrayList<>();
    private String error = null;

    private SubscriptionsLoader(XProgressDialog waitDialog, AzureApi.ConnectionSettings connectionSettings) {
        this.waitDialog = waitDialog;
        this.connectionSettings = connectionSettings;
    }

    public static List<Subscription> downloadSubscriptions(AzureApi.ConnectionSettings connectionSettings) {
        XProgressDialog waitDialog = UISupport.getDialogs().createProgressDialog(Strings.Executing.QUERY_SUBSCRIPTIONS_PROGRESS, 100, "", true);
        SubscriptionsLoader worker = new SubscriptionsLoader(waitDialog, connectionSettings);
        try {
            worker.waitDialog.run(worker);
        } catch (Exception e) {
            UISupport.showErrorMessage(e.getMessage());
            SoapUI.logError(e);
        }
        return worker.subscriptions;
    }

    @Override
    public Object construct(XProgressMonitor xProgressMonitor) {
        try {
            List<Subscription> list = AzureApi.getSubscriptionList(connectionSettings);
            this.subscriptions.addAll(list);
        } catch (InvalidAuthorizationException e) {
            SoapUI.logError(e);
            error = Strings.Executing.QUERY_SUBSCRIPTIONS_ERROR + Strings.AzureRestApi.INVALID_AUTHORIZATION_ERROR;
        } catch (IOException e) {
            SoapUI.logError(e);
            error = Strings.Executing.QUERY_SUBSCRIPTIONS_ERROR + e.getMessage();
        }
        return null;
    }

    @Override
    public void finished() {
        if (canceled) {
            return;
        }
        waitDialog.setVisible(false);
        if (!StringUtils.isNullOrEmpty(error)) {
            UISupport.showErrorMessage(error);
        }
    }

    @Override
    public boolean onCancel() {
        canceled = true;
        waitDialog.setVisible(false);
        return true;
    }
}
