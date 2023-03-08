package org.joget.marketplace;

import java.util.Map;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.model.FormValidator;
import org.joget.apps.form.service.FormUtil;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.property.model.PropertyEditable;

public class MultiFormValidator extends FormValidator {

    @Override
    public String getName() {
        return "Multi Form Validator";
    }

    @Override
    public String getVersion() {
        return "7.0.1";
    }

    @Override
    public String getDescription() {
        return "Enable the use of multiple form validators";
    }
    
    @Override
    public String getLabel() {
        return "Multi Form Validator";
    }

    @Override
    public String getClassName() {
        return this.getClass().getName();
    }
    
    @Override
    public String getPropertyOptions() {
        AppDefinition appDef = AppUtil.getCurrentAppDefinition();
        String appId = appDef.getId();
        String appVersion = appDef.getVersion().toString();
        Object[] arguments = new Object[]{appId, appVersion};
        String json = AppUtil.readPluginResource(getClass().getName(), "/properties/multiFormValidator.json", arguments, true, "messages/multiFormValidator");
        return json;
    }

    @Override
    public boolean validate(Element element, FormData data, String[] values) {
        String[] validatorList = new String[]{"firstValidator","secondValidator","thirdValidator","fourthValidator","fifthValidator"};
        boolean validatorResult = false;
        
        int count = 0;
        String isReverse = null;
        String errorValidatorMsg = null;
        String id = FormUtil.getElementParameterName(element);
        Map<String, String> errors = data.getFormErrors();
        
        for (String validatorPropertyName : validatorList) {
            count++;
            isReverse = (String) getPropertyString("isReverse" + count);
            errorValidatorMsg = (String) getPropertyString("errorValidatorMsg" + count);
            
            Object objValidator = getProperty(validatorPropertyName);
            PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
            if (objValidator != null && objValidator instanceof Map) {
                Map fvMap = (Map) objValidator;
                if (fvMap != null && fvMap.containsKey("className") && !fvMap.get("className").toString().isEmpty()) {
                    String className = fvMap.get("className").toString();
                    FormValidator p = (FormValidator)pluginManager.getPlugin(className);

                    Map propertiesMap = (Map) fvMap.get("properties");
                    FormValidator appPlugin = (FormValidator) p;

                    if (appPlugin instanceof PropertyEditable) {
                        ((PropertyEditable) appPlugin).setProperties(propertiesMap);
                    }
                    validatorResult = appPlugin.validate(element, data, values);
                    
                    // Check if reverse is selected
                    if (isReverse.equals("true")) {
                        if (!validatorResult) {
                            validatorResult = !validatorResult;
                            errors.remove(id);
                        } else {
                            validatorResult = !validatorResult;
                            data.addFormError(id, errorValidatorMsg);
                        }
                    }
                }
            }

            if (!validatorResult) {
                return validatorResult;
            }
        }
        
        return validatorResult;
    }
    
    @Override
    public String getElementDecoration() {
        String decoration = "";
        
        Object firstValidator = getProperty("firstValidator");
        PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
        if (firstValidator != null && firstValidator instanceof Map) {
            Map fvMap = (Map) firstValidator;
            if (fvMap != null && fvMap.containsKey("className") && !fvMap.get("className").toString().isEmpty()) {
                String className = fvMap.get("className").toString();
                FormValidator p = (FormValidator) pluginManager.getPlugin(className);

                Map propertiesMap = (Map) fvMap.get("properties");
                FormValidator appPlugin = (FormValidator) p;

                if (appPlugin instanceof PropertyEditable) {
                        ((PropertyEditable) appPlugin).setProperties(propertiesMap);
                }
                return appPlugin.getElementDecoration();
            }
        }
        
        return decoration;
    }
}
