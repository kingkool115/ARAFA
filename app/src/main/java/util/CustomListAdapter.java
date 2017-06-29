package util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.RadioButton;

import com.android.pushbots.R;

import java.util.ArrayList;

/**
 * Created by gest3747 on 19.06.17.
 */

public class CustomListAdapter extends ArrayAdapter<Lecture> implements Filterable {

    private Context context;
    private ArrayList<Lecture> items;
    private ArrayList<Lecture> allItems;

    public CustomListAdapter(Context context, int resource, ArrayList<Lecture> items) {
        super(context, resource, items);
        this.items = items;
        this.allItems = items;
        this.context = context;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.listitemrow, null);
        }

        CheckBox lectureCheckbox = v.findViewById(R.id.lecture_row_id);

        Lecture lecture = items.get(position);

        lectureCheckbox.setText(lecture.getName());
        lectureCheckbox.setTag(lecture.getId());

        return v;
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                items = (ArrayList<Lecture>) results.values;
                notifyDataSetChanged();
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                // restore whole lecture list if filter is empty
                if (constraint.toString().trim().isEmpty()) {
                    items = allItems;
                }

                FilterResults results = new FilterResults();
                ArrayList<Lecture> FilteredArrayNames = new ArrayList<>();

                // perform your search here using the searchConstraint String.
                constraint = constraint.toString().toLowerCase();
                for (int i = 0; i < items.size(); i++) {
                    String lectureName = items.get(i).getName();
                    String lectureId = items.get(i).getId();
                    if (lectureName.toLowerCase().startsWith(constraint.toString()))  {
                        FilteredArrayNames.add(new Lecture(lectureName, lectureId));
                    }
                }

                results.count = FilteredArrayNames.size();
                results.values = FilteredArrayNames;

                return results;
            }
        };

        return filter;
    }
}
