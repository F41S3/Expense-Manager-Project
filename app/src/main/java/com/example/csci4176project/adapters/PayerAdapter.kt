package com.example.csci4176project.adapters


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.csci4176project.R
import com.example.csci4176project.components.CurrencyConverter
import com.example.csci4176project.repositories.UserRepository
import com.example.csci4176project.util.CurrentUser
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso

/**
 * Adapter used to display the list of payers in a particular transaction
 */
class PayerAdapter(private var participantList: Map<String, Double> = emptyMap()) : RecyclerView.Adapter<PayerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.payer_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val participant = participantList.entries.elementAt(position)
        holder.bind(participant)
    }

    override fun getItemCount(): Int = participantList.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userName = itemView.findViewById<TextView>(R.id.userName)
        val userEmail = itemView.findViewById<TextView>(R.id.userEmail)
        val amount = itemView.findViewById<TextView>(R.id.amountText)
        val pfp = itemView.findViewById<ImageView>(R.id.imageView)

        // Grab the list of participants from the expense and iterate over each to display their amount owed
        fun bind(p: Map.Entry<String, Double>) {
            UserRepository.read(p.key) { user ->
                userName.setText(user?.name)
                userEmail.setText(user?.email)

                // Format the amount to two decimal places
                val formattedAmount = String.format("%.2f", CurrencyConverter.convert(p.value, "USD", CurrentUser.currency!!))

                if (p.value != 0.0) {
                    amount.setText("OWE: $formattedAmount")
                } else {
                    amount.setText("ALREADY PAID")
                }

                val id = user?.pfp?.substringAfterLast("/")
                val imageUrl = "https://firebasestorage.googleapis.com/v0/b/csci4176project-cbc40.appspot.com/o/$id?alt=media"
                Picasso.get().load(imageUrl)
                    .error(R.drawable.default_profile_picture)
                    .fit()
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .into(pfp)
            }
        }
    }

    fun setParticipants(participantList: Map<String, Double>) {
        this.participantList = participantList
        notifyDataSetChanged()
    }

}