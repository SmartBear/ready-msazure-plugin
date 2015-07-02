package com.smartbear.msazuresupport;

public final class Strings {
    private Strings() {
    }

    public static final class PluginInfo {
        public static final String NAME = "Microsoft Azure API Management Plugin";
        public static final String DESCRIPTION = "Adds actions for importing APIs from Azure API Management";
    }

    public static final class AddApiAction {
        public static final String NAME = "Add API From Azure API Management";
        public static final String DESCRIPTION = "Imports APIs from an Azure API Management service instance";
        public static final String PROMPT_API_DIALOG_CAPTION = "Import APIs from Azure API Management";
        public static final String PROMPT_API_DIALOG_DESCRIPTION = "Enter Management REST API URL (e.g. contoso.management.azure-api.net)";
    }

    public static final class NewProjectAction {
        public static final String NAME = "Create Project From Azure API Management";
        public static final String DESCRIPTION = "Creates a new project for APIs imported from Azure API Management";
        public static final String UNABLE_CREATE_ERROR = "Failed to create the project due to %s exception with \"%s\" message";
        public static final String ACTION_CAPTION = "Azure API Management";
    }

    public static final class NewProjectDialog {
        public static final String CAPTION = "Create Project From APIs Managed by Azure API Management";
        public static final String DESCRIPTION = "Creates a new project for APIs imported from Azure API Management in this workspace";
        public static final String PROJECT_LABEL = "Project Name";
        public static final String PROJECT_DESCRIPTION = "Name of the project";
        public static final String URL_LABEL = "Management REST API URL";
        public static final String URL_DESCRIPTION = "Management REST API URL (e.g. contoso.management.azure-api.net)";
        public static final String EMPTY_PROJECT_WARNING = "Please enter project name";
        public static final String EMPTY_URL_WARNING = "Please enter the Management REST API URL";
        public static final String INVALID_URL_WARNING = "Invalid Management REST API URL";
        public static final String CREDENTIALS_LABEL = "Credentials";
        public static final String ACCESS_TOKEN_LABEL = "Access Token";
        public static final String ACCESS_TOKEN_DESCRIPTION = "Management REST API access token";
        public static final String EMPTY_ACCESS_TOKEN_WARNING = "Please enter access token";
        public static final String INVALID_ACCESS_TOKEN_WARNING = "Please enter valid access token";
    }

    public static final class SelectApiDialog {
        public static final String CAPTION = "Select APIs to Import";
        public static final String DESCRIPTION = "Please select APIs you would like to import into the project from the list below";
        public static final String NAME_LABEL = "API Name";
        public static final String DESCRIPTION_LABEL = "API Description";
        public static final String DEFINITION_LABEL = "API Definition";
        public static final String GEN_TEST_SUITE = "Generate Test Suite";
        public static final String GEN_LOAD_TEST = "Generate Load Test";
        public static final String GEN_SECUR_TEST = "Generate Security Test";
        public static final String GEN_VIRT_HOST = "Generate Virtual Host";
        public static final String NOTHING_SELECTED_WARNING = "Please select at least one API to import";
    }

    public static final class SubscriptionKeyDialog {
        public static final String CAPTION = "Enter Subscription keys for the selected APIs";
        public static final String DESCRIPTION = "Please enter the Subscription keys for the selected to import APIs";
        public static final String REMARK = "A Subscription key can be set or changed later on the 'Custom properties' tab of the project";
        public static final String NAME_COLUMN = "API";
        public static final String KEY_COLUMN = "Subscription key";
    }

    public static final class Executing {
        public static final String QUERY_API_PROGRESS = "Getting APIs list...";
        public static final String QUERY_API_ERROR = "Unable to get API list from the specified Azure API Management service instance due to the following error:\n";
        public static final String QUERY_API_EMPTY_ERROR = "No API is accessible at the specified URL";

        public static final String IMPORT_PROGRESS = "Importing APIs...";
        public static final String IMPORT_ERROR = "Failed to read API description for [%s] - [%s]\n";
        public static final String IMPORT_ERROR_TAIL = "You can search and create issues for this plugin at https://github.com/SmartBear/ready-msazure-plugin/issues.";

        public static final String QUERY_SUBSCRIPTIONS_PROGRESS = "Getting subscriptions List...";
        public static final String QUERY_SUBSCRIPTIONS_ERROR = "Unable to get subscriptions list from the specified Azure API Management service instance due to the following error:\n";
    }

    public static final class AzureRestApi {
        public static final String VALUE_ENTRY_ABSENT_ERROR = "Input JSON has incorrect format: no \"value\" entry has been found.";
        public static final String UNAVAILABLE_DATA_ERROR = "No data available at the \"%s\" location. You can search and create issues for this plugin at https://github.com/SmartBear/ready-msazure-plugin/issues.";
        public static final String UNEXPECTED_RESPONSE_FORMAT_ERROR = "Unexpected response format of the request to the \"%s\" location. You can search and create issues for this plugin at https://github.com/SmartBear/ready-msazure-plugin/issues.";
        public static final String INVALID_AUTHORIZATION_ERROR = "Authorization";
    }
}
