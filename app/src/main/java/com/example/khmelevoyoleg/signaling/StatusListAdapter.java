package com.example.khmelevoyoleg.signaling;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class StatusListAdapter extends SimpleAdapter {

    private MainActivity activity;  // связывание с активностью, которая вызвала данную задачу

    StatusListAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
        super(context, data, resource, from, to);
    }

    // получаем ссылку на MainActivity
    void link(MainActivity act) {
        activity = act;
    }

    private static class ViewHolder {
        TextView tvMainStatusNumber; // номер события в списке
        TextView tvMainStatusName; // имя события в списке
        ImageView ivMaimStatus; // отображение события в списке
        TextView tvMainStatusTime; // время события
    }
 /*
    // пункт списка
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // получаем View
        StatusListAdapter.ViewHolder viewHolder;
        if (convertView == null) {
            convertView = super.getView(position, null, parent);
            viewHolder = new StatusListAdapter.ViewHolder();
            viewHolder.tvMainStatusNumber = (TextView) convertView.findViewById(R.id.tvMainStatusNumber);
            viewHolder.tvMainStatusName = (TextView) convertView.findViewById(R.id.tvMainStatusName);
            viewHolder.ivMaimStatus = (ImageView) convertView.findViewById(R.id.ivMaimStatus);
            viewHolder.tvMainStatusTime = (TextView) convertView.findViewById(R.id.tvMainStatusTime);
            // задаем Tag для группы View
            convertView.setTag(viewHolder);
            // задаем Tag для всех элементнов группы
            viewHolder.tvMainStatusNumber.setTag(R.id.tvMainStatusNumber, position);
            viewHolder.tvMainStatusName.setTag(R.id.tvMainStatusName, position);
            viewHolder.ivMaimStatus.setTag(R.id.ivMaimStatus, position);
            viewHolder.tvMainStatusTime.setTag(R.id.tvMainStatusTime, position);
            // устанавливаем значение картинки состояния входа
            viewHolder.ivMaimStatus.setImageResource(getImageViewValue(position));
        } else {
            // задаем Tag для EditText
            viewHolder = (StatusListAdapter.ViewHolder) convertView.getTag();
            // задаем Tag для всех элементнов группы
            viewHolder.tvMainStatusNumber.setTag(R.id.tvMainStatusNumber, position);
            viewHolder.tvMainStatusName.setTag(R.id.tvMainStatusName, position);
            viewHolder.ivMaimStatus.setTag(R.id.ivMaimStatus, position);
            viewHolder.tvMainStatusTime.setTag(R.id.tvMainStatusTime, position);
            // устанавливаем значение текстовых полей группы
            viewHolder.tvMainStatusNumber.setText(activity.mainStatusNumber.get(position));
            viewHolder.tvMainStatusName.setText(activity.mainStatusName.get(position));
            viewHolder.tvMainStatusTime.setText(activity.mainStatusTime.get(position));
            // устанавливаем значение картинки
            viewHolder.ivMaimStatus.setImageResource(getImageViewValue(position));
        }
        return convertView;
    }
*/
    /**
     * получение нужной картинки для вывода в ivInOutStatus
     * @param position - позиция элемента в списке
     * @return - номер ресурса
     */
    private int getImageViewValue(int position) {
        if (activity.inOutStatus.get(position).equals("STATUS_OFF")) {
            return R.drawable.circle_grey48;
        } else if (activity.inOutStatus.get(position).equals("STATUS_ON")) {
            return R.drawable.circle_green48;
        } else if (activity.inOutStatus.get(position).equals("STATUS_START_ACTIVE")) {
            return R.drawable.circle_blue48;
        } else if (activity.inOutStatus.get(position).equals("STATUS_ALARM")) {
            return R.drawable.circle_red48;
        }
        return 0;
    }
/*
        Product p = getProduct(position);

        // заполняем View в пункте списка данными из товаров: наименование, цена
        // и картинка
        ((TextView) view.findViewById(R.id.tvDescr)).setText(p.name);
        ((TextView) view.findViewById(R.id.tvPrice)).setText(p.price + "");
        ((ImageView) view.findViewById(R.id.ivImage)).setImageResource(p.image);

        CheckBox cbBuy = (CheckBox) view.findViewById(R.id.cbBox);
        // присваиваем чекбоксу обработчик
        cbBuy.setOnCheckedChangeListener(myCheckChangeList);
        // пишем позицию
        cbBuy.setTag(position);
        // заполняем данными из товаров: в корзине или нет
        cbBuy.setChecked(p.box);

        return view;
    }
            */
}
