package com.anxpp.one.core.entity;


import com.anxpp.one.components.beamazingtoday.interfaces.BatModel;
import com.orm.SugarRecord;

/**
 * todolist实体
 * Created by anxpp.com on 2017/3/22.
 */
public class ToDoListEntity extends SugarRecord implements BatModel {

    private String name;

    private Boolean isChecked = false;

    private Long createAt = 0L;

    private Long finishAt = 0L;

    public ToDoListEntity() {
    }

    public ToDoListEntity(String name, boolean isChecked) {
        this.name = name;
        this.isChecked = isChecked;
        setCreateAt(System.currentTimeMillis());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setChecked(boolean checked) {
        if (checked)
            setFinishAt(System.currentTimeMillis());
        else
            setFinishAt(0L);
        isChecked = checked;
    }

    @Override
    public boolean isChecked() {
        return isChecked;
    }

    public Boolean getChecked() {
        return isChecked;
    }

    public void setChecked(Boolean checked) {
        isChecked = checked;
    }

    @Override
    public String getText() {
        return getName();
    }

    public Long getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Long createAt) {
        this.createAt = createAt;
    }

    public Long getFinishAt() {
        return finishAt;
    }

    public void setFinishAt(Long finishAt) {
        this.finishAt = finishAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ToDoListEntity toDoListEntity = (ToDoListEntity) o;
        return isChecked == toDoListEntity.isChecked && createAt.equals(toDoListEntity.createAt) && name.equals(toDoListEntity.getName());

    }

    @Override
    public String toString() {
        return "ToDoListEntity{" +
                "name='" + name + '\'' +
                ", isChecked=" + isChecked +
                ", createAt=" + createAt +
                ", finishAt=" + finishAt +
                '}';
    }
}
