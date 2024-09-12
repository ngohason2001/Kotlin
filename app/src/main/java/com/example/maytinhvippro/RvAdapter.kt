package com.example.maytinhvippro

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView

class ButtonAdapter(
    private val buttons: List<String>, // Danh sách các button (text)
    private val clickListener: (String) -> Unit // Xử lý sự kiện khi bấm button
) : RecyclerView.Adapter<ButtonAdapter.ButtonViewHolder>() {

    class ButtonViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ButtonViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item, parent, false)
        return ButtonViewHolder(view)
    }

    override fun onBindViewHolder(holder: ButtonViewHolder, position: Int) {
        val buttonText = buttons[position]
       val btn= holder.view.findViewById<Button>(R.id.btnitem)
        btn.text = buttonText
        btn.setOnClickListener { clickListener(buttonText) }
    }

    override fun getItemCount() = buttons.size
}