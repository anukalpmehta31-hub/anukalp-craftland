package com.anukalp.craftland

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MainActivity: AppCompatActivity() {
 private val owner="anukalpmehta31@gmail.com"
 private val auth by lazy { FirebaseAuth.getInstance() }
 private val db by lazy { FirebaseFirestore.getInstance() }
 private lateinit var authPanel:LinearLayout
 private lateinit var adminPanel:LinearLayout
 private lateinit var email:EditText
 private lateinit var password:EditText
 private lateinit var title:EditText
 private lateinit var creator:EditText
 private lateinit var code:EditText
 private lateinit var description:EditText
 private lateinit var typeSpinner:Spinner
 private lateinit var listContainer:LinearLayout
 private lateinit var status:TextView

 override fun onCreate(b:Bundle?) {
  super.onCreate(b); setContentView(R.layout.activity_main)
  authPanel=findViewById(R.id.authPanel); adminPanel=findViewById(R.id.adminPanel)
  email=findViewById(R.id.email); password=findViewById(R.id.password)
  title=findViewById(R.id.title); creator=findViewById(R.id.creator); code=findViewById(R.id.code); description=findViewById(R.id.description)
  typeSpinner=findViewById(R.id.typeSpinner); listContainer=findViewById(R.id.listContainer); status=findViewById(R.id.status)
  typeSpinner.adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,listOf("maps","assets"))
  findViewById<Button>(R.id.loginBtn).setOnClickListener{login()}
  findViewById<Button>(R.id.addBtn).setOnClickListener{addListing()}
  findViewById<Button>(R.id.logoutBtn).setOnClickListener{auth.signOut(); refreshAuth()}
  refreshAuth(); loadListings()
 }
 private fun login(){
  val e=email.text.toString().trim(); val p=password.text.toString()
  if(!e.equals(owner,true)){status.text="Only the owner email can access Admin.";return}
  auth.signInWithEmailAndPassword(e,p).addOnCompleteListener{
   status.text=if(it.isSuccessful)"Owner login successful." else "Login failed: ${it.exception?.localizedMessage}"
   refreshAuth()
  }
 }
 private fun refreshAuth(){
  val ok=auth.currentUser?.email?.equals(owner,true)==true
  authPanel.visibility=if(ok)View.GONE else View.VISIBLE
  adminPanel.visibility=if(ok)View.VISIBLE else View.GONE
 }
 private fun addListing(){
  if(auth.currentUser?.email?.equals(owner,true)!=true){status.text="Owner login required.";return}
  val collection=typeSpinner.selectedItem.toString()
  val data=hashMapOf<String,Any>("title" to title.text.toString().trim(),"creator" to creator.text.toString().trim().ifBlank{"Anukalp"},"code" to code.text.toString().trim(),"description" to description.text.toString().trim(),"createdAt" to System.currentTimeMillis())
  if(data["title"].toString().isBlank()||data["code"].toString().isBlank()){status.text="Title and code are required.";return}
  db.collection(collection).add(data).addOnSuccessListener{
   status.text="Added successfully."; title.text.clear(); creator.text.clear(); code.text.clear(); description.text.clear(); loadListings()
  }.addOnFailureListener{status.text="Add failed: ${it.localizedMessage}"}
 }
 private fun loadListings(){
  listContainer.removeAllViews()
  listOf("maps" to "MAPS","assets" to "ASSETS").forEach{pair->
   db.collection(pair.first).orderBy("createdAt",Query.Direction.DESCENDING).get().addOnSuccessListener{snap->
    if(snap.isEmpty) listContainer.addView(TextView(this).apply{text="${pair.second}: No entries yet.";setPadding(0,8,0,8)})
    else snap.documents.forEach{d->listContainer.addView(TextView(this).apply{text="${d.getString("title")?:"Untitled"}\n${d.getString("creator")?:""}\nCode: ${d.getString("code")?:""}";textSize=15f;setPadding(14,16,14,16)})}
   }.addOnFailureListener{status.text="Read failed: ${it.localizedMessage}"}
  }
 }
}
