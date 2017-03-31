package com.anxpp.one.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.anxpp.one.R;
import com.anxpp.one.components.beamazingtoday.interfaces.BatModel;
import com.anxpp.one.components.beamazingtoday.listeners.BatListener;
import com.anxpp.one.components.beamazingtoday.listeners.OnCheckChangedListener;
import com.anxpp.one.components.beamazingtoday.listeners.OnItemClickListener;
import com.anxpp.one.components.beamazingtoday.listeners.OnOutsideClickedListener;
import com.anxpp.one.components.beamazingtoday.ui.adapter.BatAdapter;
import com.anxpp.one.components.beamazingtoday.ui.animator.AnimationType;
import com.anxpp.one.components.beamazingtoday.ui.animator.BatItemAnimator;
import com.anxpp.one.components.beamazingtoday.ui.callback.BatCallback;
import com.anxpp.one.components.beamazingtoday.ui.widget.BatRecyclerView;
import com.anxpp.one.core.entity.ToDoListEntity;
import com.orm.query.Select;

import java.util.List;

/**
 * TO DO LIST activity
 */
public class ToDoListActivity extends AppCompatActivity implements BatListener {

    private BatRecyclerView mRecyclerView;
    private BatAdapter mAdapter;
    private List<ToDoListEntity> toDoListEntityList;
    private BatItemAnimator mAnimator;

//    private final static String PERSIST_FLAG = "persist_for_todolist";

    private final static String TAG = ToDoListActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todolist);

        mRecyclerView = (BatRecyclerView) findViewById(R.id.bat_recycler_view);
        mAnimator = new BatItemAnimator();

        mRecyclerView.getView().setLayoutManager(new LinearLayoutManager(this));

        mRecyclerView.getView().setAdapter(mAdapter = new BatAdapter(toDoListEntityList = queryToDoList(), this, mAnimator).setOnItemClickListener(new OnItemClickListener() {
            /**
             * 节点点击事件
             * @param item 节点
             * @param position 位置
             */
            @Override
            public void onClick(BatModel item, int position) {
                Log.i(TAG,"OnItemClickListener");
                Toast.makeText(ToDoListActivity.this, item.getText() + toDoListEntityList.size(), Toast.LENGTH_SHORT).show();
            }
        }).setOnOutsideClickListener(new OnOutsideClickedListener() {
            /**
             * 元素外部点击
             */
            @Override
            public void onOutsideClicked() {
                Log.i(TAG,"OnOutsideClickedListener");
                mRecyclerView.revertAnimation();
            }
        }).setmOnCheckChangedListener(new OnCheckChangedListener() {
            /**
             * 状态改变
             */
            @Override
            public void onCheckChangedListener(ToDoListEntity toDoListEntity) {
                Log.i(TAG,"onCheckChangedListener");
                ToDoListEntity.deleteAll(ToDoListEntity.class);
                ToDoListEntity.saveInTx(toDoListEntityList);
            }
        }));

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new BatCallback(this));
        itemTouchHelper.attachToRecyclerView(mRecyclerView.getView());
        mRecyclerView.getView().setItemAnimator(mAnimator);
        mRecyclerView.setAddItemListener(this);

        findViewById(R.id.root).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRecyclerView.revertAnimation();
            }
        });
    }

    /**
     * 添加节点
     * @param string 标题
     */
    @Override
    public void add(String string) {
        ToDoListEntity toDoListEntity = new ToDoListEntity(string, false);
        ToDoListEntity.save(toDoListEntity);
        toDoListEntityList.add(0, toDoListEntity);
        mAdapter.notify(AnimationType.ADD, 0);
    }

    /**
     * 删除节点
     * @param position 节点位置
     */
    @Override
    public void delete(int position) {
        ToDoListEntity.delete(toDoListEntityList.get(position));
        toDoListEntityList.remove(position);
        mAdapter.notify(AnimationType.REMOVE, position);
    }

    /**
     * 移动节点
     * @param from 初始位置
     * @param to 目标位置
     */
    @Override
    public void move(int from, int to) {
        if (from >= 0 && to >= 0) {
            mAnimator.setPosition(to);
            ToDoListEntity toDoListEntity = toDoListEntityList.get(from);
            toDoListEntityList.remove(toDoListEntity);
            toDoListEntityList.add(to, toDoListEntity);
            mAdapter.notify(AnimationType.MOVE, from, to);

            if (from == 0 || to == 0) {
                mRecyclerView.getView().scrollToPosition(Math.min(from, to));
            }
        }
    }
    /**
     * 初始化加载数据
     * @return 数据集合
     */
    private List<ToDoListEntity> queryToDoList() {
        return Select.from(ToDoListEntity.class).orderBy("is_checked, finish_at, create_at").list();
    }
}
