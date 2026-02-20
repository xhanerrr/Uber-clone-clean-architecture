package com.example.signinregister.ui.map.searchextension

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.signinregister.R
import com.example.signinregister.domain.AddressSuggestion

class AddressSuggestionAdapter(
    private val onItemClicked: (AddressSuggestion) -> Unit = {}
) : ListAdapter<AddressSuggestion, AddressSuggestionAdapter.SuggestionViewHolder>(AddressDiffCallback()) {

    class SuggestionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val mainTextView: TextView = itemView.findViewById(R.id.tv_main_text)
        private val secondaryTextView: TextView = itemView.findViewById(R.id.tv_secondary_text)

        fun bind(suggestion: AddressSuggestion, clickListener: (AddressSuggestion) -> Unit) {
            mainTextView.text = suggestion.mainText
            secondaryTextView.text = suggestion.secondaryText

            itemView.setOnClickListener {
                clickListener(suggestion)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_address_suggestion, parent, false)
        return SuggestionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SuggestionViewHolder, position: Int) {
        holder.bind(getItem(position), onItemClicked)
    }

    private class AddressDiffCallback : DiffUtil.ItemCallback<AddressSuggestion>() {
        override fun areItemsTheSame(oldItem: AddressSuggestion, newItem: AddressSuggestion): Boolean {
            return oldItem.mainText == newItem.mainText
        }

        override fun areContentsTheSame(oldItem: AddressSuggestion, newItem: AddressSuggestion): Boolean {
            return oldItem == newItem
        }
    }
}