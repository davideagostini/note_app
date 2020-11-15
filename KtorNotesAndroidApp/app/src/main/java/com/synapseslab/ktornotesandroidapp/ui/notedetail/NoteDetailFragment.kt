package com.synapseslab.ktornotesandroidapp.ui.notedetail

import android.os.Bundle
import android.view.*
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.synapseslab.ktornotesandroidapp.R
import com.synapseslab.ktornotesandroidapp.data.local.entities.Note
import com.synapseslab.ktornotesandroidapp.other.Status
import com.synapseslab.ktornotesandroidapp.ui.BaseFragment
import com.synapseslab.ktornotesandroidapp.ui.dialogs.AddOwnerDialog
import dagger.hilt.android.AndroidEntryPoint
import io.noties.markwon.Markwon
import kotlinx.android.synthetic.main.fragment_note_detail.*

private const val ADD_OWNER_DIALOG_TAG = "ADD_OWNER_DIALOG_TAG"

@AndroidEntryPoint
class NoteDetailFragment : BaseFragment(R.layout.fragment_note_detail) {

    private val args: NoteDetailFragmentArgs by navArgs()

    private val viewModel: NoteDetailViewModel by viewModels()

    private var curNote: Note? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToObservers()
        fabEditNote.setOnClickListener {
            findNavController().navigate(
                NoteDetailFragmentDirections.actionNoteDetailFragmentToAddEditNoteFragment(args.id)
            )
        }

        if(savedInstanceState != null) {
            val addOwnerDialog = parentFragmentManager.findFragmentByTag(ADD_OWNER_DIALOG_TAG)
                    as AddOwnerDialog?
            addOwnerDialog?.setPositiveListener {
                addOwnerToCurrentNote(it)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.note_detail_menu, menu)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.miAddOwner -> showAddOwnerDialog()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showAddOwnerDialog() {
        AddOwnerDialog().apply {
            setPositiveListener {
                addOwnerToCurrentNote(it)
            }
        }.show(parentFragmentManager, ADD_OWNER_DIALOG_TAG)
    }

    private fun addOwnerToCurrentNote(email: String) {
        curNote?.let {
            viewModel.addOwnerToNote(email, it.id)
        }
    }

    private fun setMarkdownText(text: String) {
        val markwon = Markwon.create(requireContext())
        val markdown = markwon.toMarkdown(text)
        markwon.setParsedMarkdown(tvNoteContent, markdown)
    }

    private fun subscribeToObservers() {
        viewModel.addOwnerStatus.observe(viewLifecycleOwner, Observer { event ->
            event?.getContentIfNotHandled()?.let { result ->
                when (result.status) {
                    Status.SUCCESS -> {
                        addOwnerProgressBar.visibility = View.GONE
                        showSnackbar(result.data ?: "Successfully added owner to note")
                    }
                    Status.ERROR -> {
                        addOwnerProgressBar.visibility = View.GONE
                        showSnackbar(result.message ?: "An unknown error occured")
                    }

                    Status.LOADING -> {
                        addOwnerProgressBar.visibility = View.VISIBLE
                    }
                }

            }

        })
        viewModel.observeNoteById(args.id).observe(viewLifecycleOwner, Observer {
            it?.let { note ->
                tvNoteTitle.text = note.title
                setMarkdownText(note.content)
                curNote = note
            } ?: showSnackbar("Note not found")
        })
    }

}