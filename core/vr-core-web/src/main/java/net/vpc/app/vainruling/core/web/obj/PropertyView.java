/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.vpc.app.vainruling.core.web.obj;

import jersey.repackaged.com.google.common.base.Objects;
import net.vpc.common.util.Convert;
import net.vpc.upa.Entity;
import net.vpc.upa.EntityBuilder;
import net.vpc.upa.KeyType;
import net.vpc.upa.NamedId;
import net.vpc.upa.types.DataType;
import net.vpc.upa.types.EnumType;
import net.vpc.upa.types.ManyToOneType;

import javax.faces.event.ValueChangeEvent;
import javax.faces.event.ValueChangeListener;
import javax.faces.model.SelectItem;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author taha.bensalah@gmail.com
 */
public class PropertyView implements Serializable {

    protected Object value;
    protected Object selectedItem;
    private Object referrer;
    private Object rootReferrer;
    private String name;
    private String hint;
    private String header;
    private DataType dataType;
    private List<SelectItem> items = new ArrayList<>();
    //TODO change this to simple NamedId values (no need to bother of all Entity Values
    private List<NamedId> values = new ArrayList<>();
    private boolean required;
    private String componentId;
    private String separatorText;
    private String componentState = "Default";
    private boolean disabled = false;
    private String ctrlType;
    private int colspan = 1;
    private int rowpan = 1;
    private int position = 1;
    private int prependEmptyCells = 0;
    private int appendEmptyCells = 0;
    private boolean prependNewLine = false;
    private boolean appendNewLine = false;
    private boolean labelNewLine = false;
    private boolean noLabel = false;
    private boolean submitOnChange = false;
    private PropertyViewManager propertyViewManager;
    private ValueChangeListener changeListener;
    private List<PropertyView> updatablePropertyViews = new ArrayList<PropertyView>();
    private List<PropertyView> dependentPropertyViews = new ArrayList<PropertyView>();

    public PropertyView(String componentId, String name, Object referrer, String ctrlType, PropertyViewManager manager) {
        this.name = name;
        this.referrer = referrer;
        this.ctrlType = ctrlType;
        this.componentId = componentId;
        this.propertyViewManager = manager;
    }

    public PropertyViewManager getPropertyViewManager() {
        return propertyViewManager;
    }

    public String getComponentId() {
        return componentId;
    }

    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public Object getReferrer() {
        return referrer;
    }

    public void setReferrer(Object referrer) {
        this.referrer = referrer;
    }

    public String getCtrlType() {
        return ctrlType;
    }

    public void setCtrlType(String ctrlType) {
        this.ctrlType = ctrlType;
    }

    public int getColspan() {
        return colspan;
    }

    public void setColspan(int colspan) {
        this.colspan = colspan;
    }

    public int getRowpan() {
        return rowpan;
    }

    public void setRowpan(int rowpan) {
        this.rowpan = rowpan;
    }

    //        public BookProperty(Field field, String name, Object value, boolean required) {
    //            this.field = field;
    //            this.name = name;
    //            this.value = value;
    //            this.required = required;
    //        }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Integer getValueInteger() {
        Object v = getValue();
        return v == null ? null : Convert.toInt(v);
    }

    public void setValueInteger(Integer v) {
        setValue(v);
    }

    public Long getValueLong() {
        Object v = getValue();
        return v == null ? null : Convert.toLong(v);
    }

    public void setValueLong(Long v) {
        setValue(v);
    }

    public Double getValueDouble() {
        Object v = getValue();
        return v == null ? null : Convert.toDouble(v);
    }

    public void setValueDouble(Double v) {
        setValue(v);
    }

    public Float getValueFloat() {
        Object v = getValue();
        return v == null ? null : Convert.toFloat(v);
    }

    public void setValueFloat(Float v) {
        setValue(v);
    }

    public Object getFormattedValue() {
        if (value instanceof java.util.Date) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("d MMM yyyy");
            return simpleDateFormat.format(value);
        }
        return value;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public List<SelectItem> getItems() {
        return items;
    }

    public void setItems(List<SelectItem> items) {
        this.items = items;
    }

    public List<NamedId> getValues() {
        return values;
    }

    public void setValues(List<NamedId> values) {
        this.values = values;
    }

    public Object getSelectedItem() {
        return selectedItem;
    }

    public void setSelectedItem(Object selectedItem) {
        boolean someUpdates = !Objects.equal(this.selectedItem, selectedItem);
        this.selectedItem = selectedItem;
        this.value = null;
        if (selectedItem != null) {
            DataType dataType = getDataType();
            if (dataType != null) {
                if (dataType instanceof ManyToOneType) {
                    ManyToOneType et = (ManyToOneType) dataType;
                    Entity masterEntity = et.getRelationship().getTargetRole().getEntity();
                    EntityBuilder mconverter = masterEntity.getBuilder();
                    for (NamedId v : values) {
                        Object id = v.getId();
                        if (Objects.equal(Convert.toString(selectedItem), Convert.toString(id))) {
                            someUpdates |= !Objects.equal(value, v);
                            value = v;
                            break;
                        }
                    }
                } else if (dataType instanceof KeyType) {
                    KeyType et = (KeyType) dataType;
                    Entity masterEntity = et.getEntity();
                    EntityBuilder mbuilder = masterEntity.getBuilder();
                    for (Object v : values) {
                        Object id = (v instanceof NamedId) ? ((NamedId) v).getId() : mbuilder.objectToId(v);
                        if (Objects.equal(Convert.toString(selectedItem), Convert.toString(id))) {
                            someUpdates |= !Objects.equal(value, v);
                            value = v;
                            break;
                        }
                    }
                } else if (dataType instanceof EnumType) {
                    EnumType et = (EnumType) dataType;
                    for (Object v : et.getValues()) {
                        if (Objects.equal(Convert.toString(selectedItem), Convert.toString(v))) {
                            someUpdates |= !Objects.equal(value, v);
                            value = v;
                            break;
                        }
                    }
                } else {
                    //why?
                }
            }
        }
        if (someUpdates) {
            onChange(null);
        }
    }

    public boolean isPrependNewLine() {
        return prependNewLine;
    }

    public void setPrependNewLine(boolean prependNewLine) {
        this.prependNewLine = prependNewLine;
    }

    public boolean isAppendNewLine() {
        return appendNewLine;
    }

    public void setAppendNewLine(boolean appendNewLine) {
        this.appendNewLine = appendNewLine;
    }

    public void storeToMap(Map<String, Object> map) {

    }

    public void storeTo(Object o) {

    }

    public void loadFrom(Object o) {

    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public List<PropertyView> getUpdatablePropertyViews() {
        return updatablePropertyViews;
    }

    public void setUpdatablePropertyViews(List<PropertyView> updatablePropertyViews) {
        this.updatablePropertyViews = updatablePropertyViews;
    }

    public boolean isSubmitOnChange() {
        return submitOnChange;
    }

    public void setSubmitOnChange(boolean submitOnChange) {
        this.submitOnChange = submitOnChange;
    }

    public ValueChangeListener getChangeListener() {
        return changeListener;
    }

    public void setChangeListener(ValueChangeListener changeListener) {
        this.changeListener = changeListener;
    }

    public void onChange(ValueChangeEvent e) {
        if (changeListener != null) {
//            ObjCtrl ctrl=VRApp.getBean(ObjCtrl.class);
//            ctrl.currentViewToModel();
            changeListener.processValueChange(e);
        }
    }

    public List<PropertyView> getDependentPropertyViews() {
        return dependentPropertyViews;
    }

    public void setDependentPropertyViews(List<PropertyView> dependentPropertyViews) {
        this.dependentPropertyViews = dependentPropertyViews;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public boolean isLabelNewLine() {
        return labelNewLine;
    }

    public void setLabelNewLine(boolean labelNewLine) {
        this.labelNewLine = labelNewLine;
    }

    public boolean isNoLabel() {
        return noLabel;
    }

    public void setNoLabel(boolean noLabel) {
        this.noLabel = noLabel;
    }

    public int getPrependEmptyCells() {
        return prependEmptyCells;
    }

    public void setPrependEmptyCells(int prependEmptyCells) {
        this.prependEmptyCells = prependEmptyCells;
    }

    public int getAppendEmptyCells() {
        return appendEmptyCells;
    }

    public void setAppendEmptyCells(int appendEmptyCells) {
        this.appendEmptyCells = appendEmptyCells;
    }

    public String getSeparatorText() {
        return separatorText;
    }

    public void setSeparatorText(String separatorText) {
        this.separatorText = separatorText;
    }

    public DataType getDataType() {
        return dataType;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }

    public Object getRootReferrer() {
        return rootReferrer;
    }

    public PropertyView setRootReferrer(Object rootReferrer) {
        this.rootReferrer = rootReferrer;
        return this;
    }

    public void refresh() {

    }

    public String getComponentState() {
        return componentState;
    }

    public void setComponentState(String componentState) {
        this.componentState = componentState;
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }
}
