package herosauce.app1;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Map;


public class EditGroupName extends DialogFragment{

    public static final String MY_GROUPS = "MyGroupsFile";
    public static final String GROUP_COUNTER = "CounterFile";
    SharedPreferences sp, counterSP;
    Button buttonCancel, buttonSave;
    private EditText mGroupName;
    private Boolean existing_group;
    private Integer existing_group_id;
    private String existingGroupName;

    public interface UserDialog {
        void onFinishUserDialog(String user);
    }

    public EditGroupName() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.edit_group_name, container);
        mGroupName = (EditText) view.findViewById(R.id.group_name);
        String nullName = "NULL";
        existing_group = false;
        counterSP = this.getActivity().getSharedPreferences(GROUP_COUNTER, Context.MODE_PRIVATE);
        final SharedPreferences.Editor counterEditor = counterSP.edit();

        if (!nullName.equals(this.getArguments().getString("GroupName"))){
            existingGroupName = this.getArguments().getString("GroupName");
            mGroupName.setText(existingGroupName);
            existing_group = true;
            existing_group_id = counterSP.getInt(existingGroupName, 2);
        } else {
            Log.i("TAG", "No bundle arguments, moving on.");
        }

        buttonSave = (Button) view.findViewById(R.id.save_group_name);
        buttonCancel= (Button) view.findViewById(R.id.cancel_edit_group_name);

        sp = this.getActivity().getSharedPreferences(MY_GROUPS, Context.MODE_PRIVATE);
        //Going to set a counter number to zero.

        if (!counterSP.contains("default_counter")){
            counterEditor.putInt("default_counter", 2);
            counterEditor.apply();
        }

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sp.edit();
                String groupName = mGroupName.getText().toString();
                //Need to give each group a unique ID
                //then, each group number needs to be set as a group holder layout ID
                //Also, if this group already has an ID, it needs to maintain the same one
                editor.putString(groupName, groupName);
                editor.apply();

                //see if group exists (meaninig, user is just editing the group name)
                //if not, need to assign group ID and notch up the ID generator
                if (existing_group){
                    //need to get previous group ID and reassign it to new group name
                    counterEditor.putInt(groupName, existing_group_id).apply();
                    //iterate through all contacts in existing group and add them to this new one.
                    final SharedPreferences existingGroupSP = getActivity().getSharedPreferences(existingGroupName, Context.MODE_PRIVATE);
                    final SharedPreferences newGroupSP = getActivity().getSharedPreferences(groupName, Context.MODE_PRIVATE);
                    Map<String, ?> groupContacts = existingGroupSP.getAll();
                    for (final Map.Entry<String, ?> contact : groupContacts.entrySet()) {
                        String contactNumber = contact.getValue().toString();
                        String contactName = contact.getKey();
                        //remove all contacts from that old one, for good measure
                        existingGroupSP.edit().remove(contactName).apply();
                        //Now add them to the new group SP file
                        newGroupSP.edit().putString(contactName, contactNumber).apply();
                    }
                    //Need to remove the counter ID from being associated with the old group name. Need to remove that old group from the counter and group SP files.
                    counterEditor.remove(existingGroupName).apply();
                    editor.remove(existingGroupName).apply();
                } else {
                    Integer group_id = counterSP.getInt("default_counter", 0);
                    counterEditor.putInt(groupName, group_id);
                    group_id += 1;
                    counterEditor.putInt("default_counter", group_id);
                    counterEditor.apply();

                }

                Toast.makeText(getActivity(), "saved!", Toast.LENGTH_SHORT).show();

                getDialog().dismiss();
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "well, nevermind then.", Toast.LENGTH_SHORT).show();
                getDialog().dismiss();
            }
        });

        return view;
    }

    @Override
    public void onDismiss(final DialogInterface dialog){
        super.onDismiss(dialog);
        final Activity activity = getActivity();
        if (activity instanceof DialogInterface.OnDismissListener){
            ((DialogInterface.OnDismissListener) activity).onDismiss(dialog);
        }
    }
}
