package herosauce.app1;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class EditMessages extends DialogFragment {

    //TODO: for v.2, add a character counter to the dialog so users know how much room they have per SMS
    //TODO: for v.2 update, subtract google gps prefix from the total count
    public static final String MY_MESSAGES = "MyMessagesFile";
    SharedPreferences sp;
    private boolean existingMessage;
    Button bSave, bCancel;
    private EditText mTitle, mBody;
    private String existingMessageTitle;

    public interface UserDialog {
        void onFinishUserDialog(String user);
    }

    public EditMessages() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_edit_messages, container);
        mTitle = (EditText) view.findViewById(R.id.messageTitle);
        mBody = (EditText) view.findViewById(R.id.messageBody);
        String nullTitle = "NULL";
        existingMessage = false;


        if (!nullTitle.equals(this.getArguments().getString("Title"))){
            existingMessageTitle = this.getArguments().getString("Title");
            mTitle.setText(existingMessageTitle);
            String existingMessageBody = this.getArguments().getString("Message");
            mBody.setText(existingMessageBody);
            existingMessage = true;
        } else {
            Log.i("TAG", "No bundle arguments, moving on.");
        }


        bSave = (Button) view.findViewById(R.id.bSave);
        bCancel= (Button) view.findViewById(R.id.bCancel);

        sp = this.getActivity().getSharedPreferences(MY_MESSAGES, Context.MODE_PRIVATE);

        bSave.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                SharedPreferences.Editor editor = sp.edit();
                if (existingMessage){
                    editor.remove(existingMessageTitle).apply();
                }

                String title = mTitle.getText().toString();
                String messageBody = mBody.getText().toString();

                editor.putString(title, messageBody);
                editor.apply();

                Toast.makeText(getActivity(), "saved!", Toast.LENGTH_SHORT).show();

                getDialog().dismiss();
            }
        });

        bCancel.setOnClickListener(new View.OnClickListener() {
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

    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        // Return input text to activity
        UserDialog activity = (UserDialog) getActivity();
        activity.onFinishUserDialog(mTitle.getText().toString());
        this.dismiss();
        return true;
    }
}
