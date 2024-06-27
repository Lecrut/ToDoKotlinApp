package com.example.todoapp

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.todoapp.database.Todo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TodoAdapter(private val context: Context,val listener: TodoClickListener):
    RecyclerView.Adapter<TodoAdapter.TodoViewHolder>(), Filterable {

    private val todoList = ArrayList<Todo>()
    private var filterTodoList = ArrayList<Todo>()
    var newCategory: Int = 0
    var newStatus: Int = 0
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoAdapter.TodoViewHolder {
        return TodoViewHolder(
            LayoutInflater.from(context).inflate(R.layout.single_item, parent, false)
        )
    }

    init {
        if (newCategory == 0)
            filterTodoList = todoList
        else filterTodoList = todoList.filter { it.category == newCategory } as ArrayList<Todo>
    }

    override fun onBindViewHolder(holder: TodoAdapter.TodoViewHolder, position: Int) {
        val item = filterTodoList[position]
        holder.title.text = item.title
        holder.title.isSelected = true
        holder.note.text = item.note
        holder.date.text = item.date
        holder.date.isSelected = true
        holder.todo_layout.setOnClickListener {
            listener.onItemClicked(filterTodoList[holder.adapterPosition])
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

    private fun parseDate(date: String): Date {
        val format = SimpleDateFormat("dd.MM.yyyy HH:mm")
        return try {
            format.parse(date) ?: Date(0, 0, 1)
        } catch (e: Exception) {
            Date(0, 0, 1)
        }
    }

    fun updateList(newList: List<Todo>){
        todoList.clear()
        todoList.addAll(newList.sortedByDescending {parseDate(it.execution ?: "") })
        filterItemsByNewCategory(newCategory, newStatus)
        notifyDataSetChanged()
    }

    fun filterItemsByNewCategory(selectedItemPosition: Int, selectedNotification: Int) {
        newCategory = selectedItemPosition
        newStatus = selectedNotification

        var tempTodoList = todoList

        if (newStatus == 1)
            tempTodoList = todoList.filter { it.status == false  } as ArrayList<Todo>

        if (newCategory == 0)
            filterTodoList = tempTodoList
        else filterTodoList = tempTodoList.filter { it.category == newCategory } as ArrayList<Todo>

        notifyDataSetChanged()
        Log.d("filtr", newStatus.toString())
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