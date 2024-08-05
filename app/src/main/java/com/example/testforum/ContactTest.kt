package com.example.testforum

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

@Composable
fun AddContact(modifier: Modifier = Modifier) {
    val database = Firebase.database
//    val myRef = database.getReference()

    var name by remember {mutableStateOf("")}
    var email by remember {mutableStateOf("")}
    var phone by remember {mutableStateOf("")}

    Column(modifier = Modifier.padding(16.dp)){
        TextField(
            value = name,
            onValueChange = {newText -> name = newText},
            label = {Text("Name")}
        )
        TextField(
            value = email,
            onValueChange = {newText -> email = newText},
            label = {Text("Email")}
        )
        TextField(
            value = phone,
            onValueChange = {newText -> phone = newText},
            label = {Text("Phone")},
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        val context = LocalContext.current
        Button(
            onClick = {
//                myRef.setValue(text)
                val contactsRef = database.reference.child("Contacts")
                val contactRef = contactsRef.child(name)
                val contact = Contact(email, phone)
                contactRef.setValue(contact)
                Toast.makeText(context, "Save Contact", Toast.LENGTH_SHORT).show()
                name  = ""
                email = ""
                phone = ""
            },
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Save")
        }
    }
}

data class Contact(val email: String, val phone: String)