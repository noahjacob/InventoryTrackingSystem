package com.example.tablayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class InAdapter extends FirestoreRecyclerAdapter<Initem,InAdapter.itemholder> {
    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public InAdapter(@NonNull FirestoreRecyclerOptions<Initem> options) {
        super(options);
    }
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    String user_id = FirebaseAuth.getInstance().getUid();

    @Override
    protected void onBindViewHolder(@NonNull itemholder itemholder, int i, @NonNull Initem initem) {
        itemholder.textname.setText(initem.getItemname());
        itemholder.textcount.setText(String.valueOf(initem.getCount()));
        itemholder.textdesc.setText(initem.getDesc());
        itemholder.textdate.setText("Date : "+initem.getDate());
        itemholder.textid.setText("ID : "+initem.getId());
        itemholder.textloc.setText("Location : "+initem.getLocation());


    }

    @NonNull
    @Override
    public itemholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.in_layout,parent,false);
        return new itemholder(v);
    }
    public void deleteItem(int position) {
      final CollectionReference ref= db.collection("Inventory").document(user_id).collection("Items");


        final DocumentReference snapref =  getSnapshots().getSnapshot(position).getReference();
        getSnapshots().getSnapshot(position).getReference().get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot document = task.getResult();
                Log.d("Snapshot", String.valueOf(document.get("count")));
                String snapid = String.valueOf(document.get("id"));
                final int snapcount = Integer.valueOf(document.get("count").toString());
                ref.whereEqualTo("id",snapid).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        for (QueryDocumentSnapshot document : task.getResult()){
                            int itemcount = Integer.valueOf(document.get("count").toString());
                            if(itemcount-snapcount==0){
                                ref.document(document.getId()).delete();
                                snapref.delete();

                            }
                            else{
                                Map<Object, Integer> map = new HashMap<>();
                                map.put("count",itemcount-snapcount );
                                db.collection("Inventory").document(user_id)
                                        .collection("Items")
                                        .document(document.getId()).set(map, SetOptions.merge());
                                snapref.delete();
                            }
                        }

                    }
                });


            }
        });






    }
    class itemholder extends RecyclerView.ViewHolder{
        //Item view layout components
        TextView textname,textdesc,textcount,textdate,textid,textloc;

        public itemholder(@NonNull View itemView) {
            super(itemView);
            textname = itemView.findViewById(R.id.text_item);
            textdesc = itemView.findViewById(R.id.text_Desc);
            textcount = itemView.findViewById(R.id.text_count);
            textdate = itemView.findViewById(R.id.text_date);
            textid = itemView.findViewById(R.id.text_id);
            textloc = itemView.findViewById(R.id.text_loc);


        }
    }
}
