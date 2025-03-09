package com.example.diaguard;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diaguard.db.GlucoseEntry;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GlucoseHistoryAdapter extends RecyclerView.Adapter<GlucoseHistoryAdapter.ViewHolder> {

    private final List<GlucoseEntry> data;
    private final OnItemActionListener actionListener;

    public GlucoseHistoryAdapter(List<GlucoseEntry> data, OnItemActionListener actionListener) {
        this.data = data;
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public GlucoseHistoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_glucose_entry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GlucoseHistoryAdapter.ViewHolder holder, int position) {
        GlucoseEntry entry = data.get(position);

        holder.glucoseValue.setText(String.format(Locale.getDefault(), "%.1f mmol/l", entry.level));

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM", Locale.getDefault());
        holder.glucoseTime.setText(sdf.format(new Date(entry.timestamp)));

        // Закрашиваем фон itemView (или любой другой Layout внутри ViewHolder)
        int color = Util.getGlucoseColor(entry.level);
        holder.itemView.setBackgroundColor(color);

        // Долгое нажатие с PopupMenu
        holder.itemView.setOnLongClickListener(v -> {
            if (actionListener == null) {
                return false;
            }
            PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
            popupMenu.inflate(R.menu.context_menu_glucose);

            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_delete) {
                    actionListener.onDelete(entry);
                    return true;
                }
                if (item.getItemId() == R.id.action_modify_note) {
                    actionListener.onNoteModify(entry);
                    return true;
                }
                return false;
            });
            popupMenu.show();
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView glucoseValue;
        TextView glucoseTime;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            glucoseValue = itemView.findViewById(R.id.glucoseValue);
            glucoseTime = itemView.findViewById(R.id.glucoseTime);
        }
    }
}
