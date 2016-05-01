/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.vainruling.api.web.obj;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.vpc.app.vainruling.api.EntityAction;
import net.vpc.app.vainruling.api.VrApp;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 *
 * @author vpc
 */
@Component
public class ActionDialogManager {
    
    
    private Map<String, ActionDialogAdapter> byActionName;
    private Map<String, List<ActionDialogAdapter>> byEntityName = new HashMap<>();

    public ActionDialogAdapter findAction(String name) {
        return getByActionName().get(name);
    }

    public synchronized List<ActionDialogAdapter> findActionsByEntity(String entityName) {

        List<ActionDialogAdapter> list = byEntityName.get(entityName);
        if (list == null) {
            list = new ArrayList<>();
            for (ActionDialogAdapter value : getByActionName().values()) {
                if (value.acceptEntity(entityName)) {
                    list.add(value);
                }
            }
            byEntityName.put(entityName, list);
        }
        return list;
    }

    public synchronized Map<String, ActionDialogAdapter> getByActionName() {
        if (byActionName == null) {
            byActionName = new HashMap<>();
            ApplicationContext c = VrApp.getContext();
            String[] beanNames = c.getBeanNamesForAnnotation(EntityAction.class);
            for (String beanName : beanNames) {
                Object o = c.getBean(beanName);
                if (o instanceof ActionDialog) {
                    ActionDialog a = (ActionDialog) o;
                    ActionDialogAdapter aa = new ActionDialogAdapter(a);
                    if (byActionName.containsKey(aa.getId())) {
                        throw new IllegalArgumentException("Ambigous name " + aa.getId());
                    }
                    byActionName.put(aa.getId(), aa);
                } else {
                    throw new IllegalArgumentException(o.getClass() + " must implement " + ActionDialog.class);
                }
            }
        }
        return byActionName;
    }

    
   
}