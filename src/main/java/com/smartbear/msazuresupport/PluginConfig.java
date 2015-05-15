package com.smartbear.msazuresupport;

import com.eviware.soapui.plugins.PluginAdapter;
import com.eviware.soapui.plugins.PluginConfiguration;

//TODO: set valid infoUrl
@PluginConfiguration(groupId = "com.smartbear.plugins", name = "MS Azure Plugin", version = "1.0",
        autoDetect = true, description = "Adds actions to import APIs from MS Azure hosted developer portals",
        infoUrl = "" )
public final class PluginConfig extends PluginAdapter {
}
