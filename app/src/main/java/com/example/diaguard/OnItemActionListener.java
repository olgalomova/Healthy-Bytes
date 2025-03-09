package com.example.diaguard;

import com.example.diaguard.db.GlucoseEntry;

public interface OnItemActionListener {
    void onDelete(GlucoseEntry entry);
    void onNoteModify(GlucoseEntry entry);
}
