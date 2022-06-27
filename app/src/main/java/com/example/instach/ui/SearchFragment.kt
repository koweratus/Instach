package com.example.instach.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instach.adapter.UserAdapter
import com.example.instach.model.User
import com.example.instach.databinding.FragmentSearchBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private var recyclerView: RecyclerView? = null
    private var userAdapter: UserAdapter? = null
    private var mUser: MutableList<User>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        val root: View = binding.root

        recyclerView = binding.rvSearch
        recyclerView?.setHasFixedSize(true)
        recyclerView?.layoutManager = LinearLayoutManager(context)

        mUser = ArrayList()
        userAdapter = context?.let{ UserAdapter(it, mUser as ArrayList<User>, true) }
        recyclerView?.adapter = userAdapter

        binding.etSearch.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (binding.etSearch.text.toString().isEmpty()){
                }
                else{
                    recyclerView?.visibility = View.VISIBLE
                    retrieveUsers()
                    searchUser(s.toString().lowercase())
                }
            }
            override fun afterTextChanged(p0: Editable?) {
            }
        })
        return root
    }

    private fun searchUser(input: String) {
        val query = FirebaseDatabase.getInstance().reference.child("Users")
            .orderByChild("fullname").startAt(input).endAt(input + "\uf8ff")

        query.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot){
                mUser?.clear()

                    for (snapshot in dataSnapshot.children){
                        val user = snapshot.getValue(User:: class.java)
                        if (user != null){
                            mUser?.add(user)
                        }
                    }
                    userAdapter?.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun retrieveUsers() {
        val usersRef = FirebaseDatabase.getInstance().reference.child("Users")
        usersRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot){
                if (binding.etSearch.text.toString().isEmpty()){
                    mUser?.clear()

                    for (snapshot in dataSnapshot.children){
                        val user = snapshot.getValue(User:: class.java)
                        if (user != null){
                            mUser?.add(user)
                        }
                    }
                    userAdapter?.notifyDataSetChanged()
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
