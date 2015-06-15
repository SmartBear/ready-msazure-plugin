package com.smartbear.msazuresupport;

public final class Strings {
    private Strings() {
    }

    public static final class PluginInfo {
        public static final String NAME = "MS Azure Plugin";
        public static final String DESCRIPTION = "Adds actions to import APIs from MS Azure hosted developer portals";
    }

    public static final class AddApiAction {
        public static final String NAME = "Add API From MS Azure";
        public static final String DESCRIPTION = "Adds API from the MS Azure developer portal.";
        public static final String PROMPT_API_DIALOG_CAPTION = "Add API Specification from MS Azure";
        public static final String PROMPT_API_DIALOG_DESCRIPTION = "Input developer portal URL (i.e. developer.management.azure-api.net)";
    }

    public static final class NewProjectAction {
        public static final String NAME = "Create Project From MS Azure";
        public static final String DESCRIPTION = "Creates a new project from API specification on MS Azure developer portal.";
        public static final String UNABLE_CREATE_ERROR = "Unable to create Project because of %s exception with \"%s\" message";
        public static final String ACTION_CAPTION = "MS Azure Developer Portal (REST)";
    }

    public static final class NewProjectDialog {
        public static final String CAPTION = "Create Project From API Specification on MS Azure Portal";
        public static final String DESCRIPTION = "Creates a new Project from API specification on MS Azure developer portal in this workspace";
        public static final String PROJECT_LABEL = "Project Name";
        public static final String PROJECT_DESCRIPTION = "Name of the project";
        public static final String URL_LABEL = "Developer Portal URL";
        public static final String URL_DESCRIPTION = "Developer portal URL (i.e. developer.management.azure-api.net)";
        public static final String EMPTY_PROJECT_WARNING = "Please enter project name.";
        public static final String EMPTY_URL_WARNING = "Please enter the developer portal URL.";
        public static final String INVALID_URL_WARNING = "Invalid developer portal URL.";
        public static final String CREDENTIALS_LABEL = "Credentials";
        public static final String ACCESS_TOKEN_LABEL = "Access Token";
        public static final String ACCESS_TOKEN_DESCRIPTION = "API Management REST API Access Token";
        public static final String EMPTY_ACCESS_TOKEN_WARNING = "Please enter Access Token.";
        public static final String INVALID_ACCESS_TOKEN_WARNING = "Please enter valid Access Token.";
    }

    public static final class SelectApiDialog {
        public static final String CAPTION = "Select API to Import";
        public static final String DESCRIPTION = "Please select from the list which API specification(s) you want to import to the project.";
        public static final String NAME_LABEL = "API Name";
        public static final String DESCRIPTION_LABEL = "API Description";
        public static final String DEFINITION_LABEL = "API Definition";
        public static final String GEN_TEST_SUITE = "Generate TestSuite";
        public static final String GEN_LOAD_TEST = "Generate LoadTest";
        public static final String GEN_SECUR_TEST = "Generate Security Test";
        public static final String GEN_VIRT_HOST = "Generate Virtual Host";
        public static final String NOTHING_SELECTED_WARNING = "Please select at least one API specification to add.";
    }

    public static final class Executing {
        public static final String QUERY_API_PROGRESS = "Getting APIs List...";
        public static final String QUERY_API_ERROR = "Unable to read API list from the specified MS Azure developer portal because of the following error:\n";
        public static final String QUERY_API_EMPTY_ERROR = "No API is accessible at the specified URL.";

        public static final String IMPORT_PROGRESS = "Importing APIs...";
        public static final String IMPORT_ERROR = "Failed to read API description for [%s] - [%s]\n";
        public static final String IMPORT_ERROR_TAIL = "Please contact MS Azure support for assistance";
    }

    public static final class AzureRestApi {
        public static final String UNAVAILABLE_API_ERROR = "No APIs available at this location. Please contact MS Azure support for assistance.";
        public static final String INVALID_RESPONSE_FORMAT_ERROR = "The list of APIs is not in the expected JSON format. Please contact MS Azure support for assistance.";
        public static final String INVALID_SPECIFICATION_ERROR = "API specification list has incorrect format: no \"value\" entry has been found.\nPlease contact MS Azure support for assistance.";
    }
}
