package com.example.signinregister.ui.map.searchextension

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.signinregister.R
import com.example.signinregister.databinding.BottomDialogExtensionBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class BottomDialogExtension : BottomSheetDialogFragment() {


    private val viewModel: BottomExtensionViewModel by viewModels()

    private var _binding: BottomDialogExtensionBinding? = null
    private val binding get() = _binding!!

    private lateinit var suggestionsAdapter: AddressSuggestionAdapter

    private var listener: LocationSelectedListener? = null

    private val DEBUG_TAG = "DIALOG_DEBUG"

    companion object {
        const val TAG = "ExtensionBottomDialog"
        private const val ARG_SEARCH_FIELD = "search_field"
        private const val ARG_CURRENT_ADDRESS = "current_address"
        private const val ARG_SECONDARY_ADDRESS = "secondary_address"

        fun newInstance(field: SearchField, currentAddress: String, secondaryAddress: String) = BottomDialogExtension().apply {
            arguments = Bundle().apply {
                putSerializable(ARG_SEARCH_FIELD, field)
                putString(ARG_CURRENT_ADDRESS, currentAddress)
                putString(ARG_SECONDARY_ADDRESS, secondaryAddress)
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = when {
            context is LocationSelectedListener -> context
            parentFragment is LocationSelectedListener -> parentFragment as LocationSelectedListener
            else -> null.also {
                Log.e(DEBUG_TAG, "ERROR: Activity no implementa LocationSelectedListener.")
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun getTheme() = R.style.FullScreenBottomSheetDialog

    private fun getWindowHeight(): Int {
        return resources.displayMetrics.heightPixels
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        dialog.setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog

            val bottomSheet = bottomSheetDialog.findViewById<View>(
                com.google.android.material.R.id.design_bottom_sheet
            ) as FrameLayout?

            bottomSheet?.let {
                val behavior = BottomSheetBehavior.from(it)

                val layoutParams = it.layoutParams
                if (layoutParams != null) {
                    layoutParams.height = getWindowHeight()
                }
                it.layoutParams = layoutParams

                behavior.isFitToContents = false
                behavior.skipCollapsed = true
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = BottomDialogExtensionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        suggestionsAdapter = AddressSuggestionAdapter(
            onItemClicked = { suggestion ->
                viewModel.getPlaceDetails(suggestion)
            }
        )
        binding.recyclerViewBottomDialog.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = suggestionsAdapter
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.suggestions.collect { suggestionsList ->
                suggestionsAdapter.submitList(suggestionsList)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.locationResult.collect { result ->
                if (result != null) {
                    listener?.onLocationSelected(result)
                    dismiss()
                    viewModel.clearLocationResult()
                }
            }
        }

        val fieldToFocus = arguments?.getSerializable(ARG_SEARCH_FIELD) as? SearchField
        val currentAddress = arguments?.getString(ARG_CURRENT_ADDRESS)
        val secondaryAddress = arguments?.getString(ARG_SECONDARY_ADDRESS)

        val editTextFrom = binding.editTxtFromBDE
        val editTextTo = binding.editTxtToBDE

        val editTexts = mapOf(
            SearchField.FROM to editTextFrom,
            SearchField.TO to editTextTo
        )

        editTexts.forEach { (field, editText) ->
            val text = if (field == SearchField.FROM) currentAddress else secondaryAddress

            if (!text.isNullOrBlank() && text != "Seleccionar ubicación...") {
                editText.setText(text)
                editText.setSelection(text.length)
            } else {
                editText.setText("")
            }

            editText.isFocusable = true
            editText.isFocusableInTouchMode = true
            editText.isCursorVisible = true
        }

        val focusAndTextListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                (v as EditText).doAfterTextChanged { editable ->
                    viewModel.searchAddress(editable.toString())
                }

                v.postDelayed({
                    val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                    imm?.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT)
                    v.setSelection(v.length())
                }, 200)

            }
        }

        editTextFrom.tag = SearchField.FROM
        editTextTo.tag = SearchField.TO

        editTextFrom.onFocusChangeListener = focusAndTextListener
        editTextTo.onFocusChangeListener = focusAndTextListener

        val targetEditText = editTexts[fieldToFocus]

        targetEditText?.requestFocus()

        binding.appCompatButton.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.editTxtFromBDE.isFocusableInTouchMode = true
        binding.editTxtToBDE.isFocusableInTouchMode = true
        binding.editTxtFromBDE.isCursorVisible = true
        binding.editTxtToBDE.isCursorVisible = true
        _binding = null
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (activity is LocationSelectedListener) {
            (activity as LocationSelectedListener).onSearchDialogDismissed()
        }
    }

}