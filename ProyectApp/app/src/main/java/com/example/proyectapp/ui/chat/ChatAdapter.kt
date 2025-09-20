package com.example.proyectapp.ui.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectapp.R
import de.hdodenhof.circleimageview.CircleImageView

// El adaptador recibe una lista de mensajes mutables
class ChatAdapter(private val messages: MutableList<Message>) :
    RecyclerView.Adapter<ChatAdapter.MessageViewHolder>() {

    // Nombres del usuario y el especialista para la lógica
    private val USER = "usuario"
    private val SPECIALIST = "especialista"

    // Define un ViewHolder para almacenar las vistas de cada elemento de la lista
    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val specialistAvatar: CircleImageView = itemView.findViewById(R.id.imageViewSpecialistAvatar)
        val userAvatar: CircleImageView = itemView.findViewById(R.id.imageViewUserAvatar)
        val messageTextView: TextView = itemView.findViewById(R.id.textViewMessage)
    }

    // Este método se llama para crear un nuevo ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    // Este método se llama para asociar los datos con las vistas del ViewHolder
    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]

        // Lógica para diferenciar entre remitentes
        if (message.sender == SPECIALIST) {
            // Es un mensaje del especialista
            holder.specialistAvatar.visibility = View.VISIBLE
            holder.userAvatar.visibility = View.GONE
            // Ajustar el texto del mensaje para el especialista
            // La alineación en el XML ya está configurada, no necesita más cambios aquí
            holder.messageTextView.background = holder.itemView.context.getDrawable(R.drawable.message_specialist_background)
        } else {
            // Es un mensaje del usuario
            holder.specialistAvatar.visibility = View.GONE
            holder.userAvatar.visibility = View.VISIBLE
            // Ajustar el texto del mensaje para el usuario
            holder.messageTextView.background = holder.itemView.context.getDrawable(R.drawable.message_user_background)
        }

        // Finalmente, establece el texto del mensaje
        holder.messageTextView.text = message.text
    }

    override fun getItemCount(): Int = messages.size

    fun updateMessages(newMessages: List<Message>) {
        messages.clear()
        messages.addAll(newMessages)
        notifyDataSetChanged()
    }
}