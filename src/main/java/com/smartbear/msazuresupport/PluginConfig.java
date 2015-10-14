package com.smartbear.msazuresupport;

import com.eviware.soapui.plugins.PluginAdapter;
import com.eviware.soapui.plugins.PluginConfiguration;

//TODO: set valid infoUrl
@PluginConfiguration(groupId = "com.smartbear.plugins", name = Strings.PluginInfo.NAME, version = "1.0.1",
        autoDetect = true, description = Strings.PluginInfo.DESCRIPTION,
        infoUrl = "")
public final class PluginConfig extends PluginAdapter {
}
