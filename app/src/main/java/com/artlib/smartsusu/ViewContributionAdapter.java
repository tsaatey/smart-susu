package com.artlib.smartsusu;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ViewContributionAdapter extends ArrayAdapter<Contribution> {

    public ViewContributionAdapter(Activity context, int resource, ArrayList<Contribution> contributions) {
        super(context, resource, 0, contributions);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.savings_history, parent, false);
        }

        Contribution currentContribution = getItem(position);

        TextView date = listItemView.findViewById(R.id.date_textView);
        date.setText(currentContribution.getContributionDate());

        TextView amount = listItemView.findViewById(R.id.amount_textView);
        amount.setText("GHS " + currentContribution.getAmountContributed());

        return listItemView;
    }
}
