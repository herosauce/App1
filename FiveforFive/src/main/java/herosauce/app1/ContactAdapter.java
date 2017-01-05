package herosauce.app1;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

public class ContactAdapter extends BaseAdapter {

    ArrayList<ContactRow> list;
    Context c;

    private ArrayList<ContactRow> privateArray;

    public ContactAdapter(Context c, ArrayList<ContactRow> list) {
        this.list = list;
        this.c = c;
        privateArray = new ArrayList<>();
        privateArray.addAll(list);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row =  inflater.inflate(R.layout.contacts_list_item,null,false);

        TextView name   = (TextView) row.findViewById(R.id.user_details_displayname);
        TextView number = (TextView) row.findViewById(R.id.user_details_phone_number);
        ImageView image = (ImageView) row.findViewById(R.id.user_details_avatar);

        ContactRow contactRow = list.get(position);

        name.setText(contactRow.name);
        number.setText(contactRow.number);

        if (contactRow.image!= null) {
            image.setImageURI(Uri.parse(contactRow.image));
        }
        return row;
    }

    public void filter(String charText){
        charText = charText.toLowerCase(Locale.getDefault());
        list.clear();
        if (charText.length()==0){
            list.addAll(privateArray);
        } else {
            for (ContactRow s : privateArray) {
                if (s.name.toLowerCase(Locale.getDefault()).contains(charText)){
                    list.add(s);
                }
            }
        }
        notifyDataSetChanged();
    }
}
