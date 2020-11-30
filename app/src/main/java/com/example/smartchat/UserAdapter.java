package com.example.smartchat;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.ArrayList;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.MyViewHolder> implements Filterable {
    Context context;
    ArrayList<UserCardView> details;
    ArrayList<UserCardView> fullDetails;
    boolean isPresent, isContactPresent = false;
    MainActivity mainActivity;


    public UserAdapter(ArrayList<UserCardView> d, Context c) {
        context = c;
        details = d;
        fullDetails = new ArrayList<>(d);
        mainActivity = new MainActivity();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.user_cardview, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        if (requestContactPermission(details.get(position).getPhone())) {
            try {
                holder.Username.setText(getContactName(details.get(position).getPhone(), context));
                holder.Name.setText(details.get(position).getName());
//                holder.Number.setText(details.get(position).getPhone());
                if(requestContactPermission(details.get(position).getPhone())){
                    isContactPresent = true;
                    holder.cardView.setVisibility(View.VISIBLE);
                }
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    getImage(details.get(position).getPhone(), holder);
                                } catch (Exception e) {

                                }

                            }
                        });
                    }
                };
                thread.start();

//            Picasso.get().load(details.get(position).getCompany_logo()).into(holder.company_logo);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View view) {

                        Intent intent = new Intent(context, Chat.class);
                        String phone = details.get(position).getPhone();
                        intent.putExtra("Phone", phone);
                        intent.putExtra("Username", getContactName(details.get(position).getPhone(), context));
                        view.getContext().startActivity(intent);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int getItemCount() {
        return details ==null ? 0 : details.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{

        TextView Username, Name, Number;
        ImageView Profile_Picture;
        CardView cardView;

        public MyViewHolder(@NonNull View itemView) {

            super(itemView);
            Username = itemView.findViewById(R.id.username);
            Name = itemView.findViewById(R.id.person_name);
            Number = itemView.findViewById(R.id.number);
            Profile_Picture = itemView.findViewById(R.id.profile_picture);
            cardView = itemView.findViewById(R.id.user_card);
        }
    }

    @Override
    public Filter getFilter() {
        return detailsFilter;
    }

    private Filter detailsFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            ArrayList<UserCardView> filteredList = new ArrayList<>();

            if (charSequence == null || charSequence.length() == 0){
                filteredList.addAll(fullDetails);
            } else {
                String filterPattern = charSequence.toString().toLowerCase().trim();

                for (UserCardView item : fullDetails){
                    try {
                        if (item.getUsername().toLowerCase().contains(filterPattern)){
                            filteredList.add(item);
                        }
                        else if (item.getUsername().toLowerCase().contains(filterPattern)){
                            filteredList.add(item);
                        }
                        else if (item.getName().toLowerCase().contains(filterPattern)){
                            filteredList.add(item);
                        }
                        else if (item.getPhone().toLowerCase().contains(filterPattern)){
                            filteredList.add(item);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }

            FilterResults filterResults = new FilterResults();
            filterResults.values = filteredList;
            return filterResults;

        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {

            details.clear();
            details.addAll((ArrayList) filterResults.values);
            notifyDataSetChanged();
        }
    };

    public void getImage(String user, final MyViewHolder holder){
        final Bitmap[] bm = new Bitmap[1];
        StorageReference mImageRef = FirebaseStorage.getInstance().getReference(user + "/Profile Picture");
        final long ONE_MEGABYTE = 1024 * 1024;
        mImageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                bm[0] = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                holder.Profile_Picture.setImageBitmap(bm[0]);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {

            }
        });
    }

    public boolean requestContactPermission(String number) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context,
                        android.Manifest.permission.READ_CONTACTS)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder((Activity) context);
                    builder.setTitle("Read Contacts permission");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setMessage("Please enable access to contacts.");
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @TargetApi(Build.VERSION_CODES.M)
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            ActivityCompat.requestPermissions((Activity) context,
                                    new String[]{android.Manifest.permission.READ_CONTACTS},
                                    1);
                        }
                    });
                    builder.show();
                } else {
                    ActivityCompat.requestPermissions((Activity) context,
                            new String[]{android.Manifest.permission.READ_CONTACTS},
                            1);
                    new Handler().postDelayed((new Runnable() {
                        @Override
                        public void run() {
                            ((Activity)context).recreate();
                        }
                    }), 3000);
                }
            } else {
                isPresent = contactExists((Activity) context, number);
            }
        } else {
            isPresent = contactExists((Activity) context, number);
        }
        return isPresent;
    }
    public boolean contactExists(Context context, String number) {
        /// number is the phone number
        Uri lookupUri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(number));
        String[] mPhoneNumberProjection = { ContactsContract.PhoneLookup._ID, ContactsContract.PhoneLookup.NUMBER, ContactsContract.PhoneLookup.DISPLAY_NAME };
        Cursor cur = context.getContentResolver().query(lookupUri,mPhoneNumberProjection, null, null, null);
        try {
            if (cur.moveToFirst()) {
                cur.close();
                return true;
            }
        } finally {
            if (cur != null)
                cur.close();
        }
        return false;
    }

    public String getContactName(final String phoneNumber, Context context) {
        Uri uri=Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,Uri.encode(phoneNumber));
        String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME};
        String contactName="";
        Cursor cursor=context.getContentResolver().query(uri,projection,null,null,null);
        if (cursor != null) {
            if(cursor.moveToFirst()) {
                contactName=cursor.getString(0);
            }
            cursor.close();
        }
        return contactName;
    }

    public boolean isContactPresent() {
        return isContactPresent;
    }

}
