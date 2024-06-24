package com.example.todoapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.todoapp.database.Todo
import java.util.Locale

class TodoAdapter(private val context: Context,val listener: TodoClickListener):
    RecyclerView.Adapter<TodoAdapter.TodoViewHolder>(), Filterable {

    private val todoList = ArrayList<Todo>()
    private var filterTodoList = ArrayList<Todo>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoAdapter.TodoViewHolder {
        return TodoViewHolder(
            LayoutInflater.from(context).inflate(R.layout.single_item, parent, false)
        )
    }

    init {
        filterTodoList = todoList
    }

    override fun onBindViewHolder(holder: TodoAdapter.TodoViewHolder, position: Int) {
        val item = filterTodoList[position]
        holder.title.text = item.title
        holder.title.isSelected = true
        holder.note.text = item.note
        holder.date.text = item.date
        holder.date.isSelected = true
        holder.todo_layout.setOnClickListener {
            listener.onItemClicked(todoList[holder.adapterPosition])
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                if (charSearch.isEmpty()) {
                    filterTodoList = todoList
                } else {
                    val resultList = ArrayList<Todo>()
                    for (row in todoList) {
                        if (row.title.toString().lowercase(Locale.ROOT)
                                .contains(charSearch.lowercase(Locale.ROOT))
                        ) {
                            resultList.add(row)
                        }
                    }
                    filterTodoList = resultList
                }
                val filterResults = FilterResults()
                filterResults.values = filterTodoList
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filterTodoList = results?.values as ArrayList<Todo>
                notifyDataSetChanged()
            }

        }
    }

    override fun getItemCount(): Int {
        return filterTodoList.size
    }

    fun updateList(newList: List<Todo>){
        todoList.clear()
        todoList.addAll(newList)
        notifyDataSetChanged()
    }

    inner class TodoViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val todo_layout = itemView.findViewById<CardView>(R.id.card_layout)
        val title = itemView.findViewById<TextView>(R.id.tv_title)
        val note = itemView.findViewById<TextView>(R.id.tv_note)
        val date = itemView.findViewById<TextView>(R.id.tv_date)
    }

    interface TodoClickListener {
        fun onItemClicked(todo: Todo)
    }
}