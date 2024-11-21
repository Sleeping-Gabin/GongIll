package com.example.myapplication.view.ui

import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.VERTICAL
import com.example.myapplication.R
import com.example.myapplication.databinding.PredictResultFragmentBinding
import com.example.myapplication.objects.PredictResult
import com.example.myapplication.view.adapter.PredictResultAdapter
import com.example.myapplication.view.model.MyViewModel
import com.google.android.material.color.MaterialColors

class PredictResultFragment: Fragment() {
    private val model: MyViewModel by activityViewModels()
    private val args: PredictResultFragmentArgs by navArgs()
    private lateinit var binding: PredictResultFragmentBinding

    private lateinit var result: PredictResult
    private var mode= PredictResultAdapter.Mode.WIN

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            Navigation.findNavController(requireActivity(), R.id.hostFragment).navigateUp()
        }

        binding = PredictResultFragmentBinding.inflate(inflater)

        result = args.predictResult

        binding.predictResultView.layoutManager =
            LinearLayoutManager(requireContext(), VERTICAL, false)
        val adapter = PredictResultAdapter(result)
        binding.predictResultView.adapter = adapter

        binding.predictResultBtns.addOnButtonCheckedListener { group, checkedId, isChecked ->
            when (checkedId) {
                R.id.predictResult_win -> {
                    if (isChecked) {
                        mode = PredictResultAdapter.Mode.WIN
                        adapter.changeMode(mode)
                        setPredictText()
                    }

                }
                R.id.predictResult_round -> {
                    if (isChecked) {
                        mode = PredictResultAdapter.Mode.ROUND
                        adapter.changeMode(mode)
                        setPredictText()
                    }
                }
            }

        }

        setPredictText()

        return binding.root
    }

    private fun setPredictText() {
        if (result.reverse) {
            if (result.winChance.size==0 && result.roundChance.size==0) {
                binding.predictResultTxt.text = getString(R.string.predict_reverse_null, args.team, args.rank)
                binding.predictResultWin.visibility = View.GONE
                binding.predictResultRound.visibility = View.GONE
            }
            else if (result.winChance.size == 0) {
                binding.predictResultTxt.text = getString(R.string.predict_reverse_rnd, args.team, args.rank)
                binding.predictResultWin.visibility = View.GONE
                binding.predictResultBtns.check(R.id.predictResult_round)
            }
            else if (result.roundChance.size == 0) {
                val text = getString(R.string.predict_reverse_win, args.team, args.rank)
                val color = MaterialColors.getColor(binding.root, com.google.android.material.R.attr.colorError)
                val spannable = SpannableString(text)
                spannable.setSpan(ForegroundColorSpan(color), 7, 12, 0)
                binding.predictResultTxt.text = spannable
                binding.predictResultRound.visibility = View.GONE
            }
            else {
                when (mode) {
                    PredictResultAdapter.Mode.WIN -> {
                        val text = getString(R.string.predict_reverse_check, args.team, args.rank)
                        val color = MaterialColors.getColor(binding.root, com.google.android.material.R.attr.colorError)
                        val spannable = SpannableString(text)
                        spannable.setSpan(ForegroundColorSpan(color), 7, 12, 0)
                        binding.predictResultTxt.text = spannable
                    }
                    PredictResultAdapter.Mode.ROUND -> {
                        binding.predictResultTxt.text = getString(R.string.predict_check_rnd, args.team, args.rank)
                    }
                }

            }
        }
        else {
            if (result.winChance.size == 0 && result.roundChance.size == 0) {
                binding.predictResultTxt.text =
                    getString(R.string.predict_null, args.team, args.rank)
                binding.predictResultWin.visibility = View.GONE
                binding.predictResultRound.visibility = View.GONE
            } else if (result.winChance.size == 0) {
                val text = getString(R.string.predict_rnd, args.team, args.rank)
                val color = MaterialColors.getColor(
                    binding.root,
                    com.google.android.material.R.attr.colorError
                )
                val spannable = SpannableString(text)
                spannable.setSpan(ForegroundColorSpan(color), text.length - 7, text.length - 4, 0)
                binding.predictResultTxt.text = spannable
                binding.predictResultWin.visibility = View.GONE
                binding.predictResultBtns.check(R.id.predictResult_round)
            } else if (result.roundChance.size == 0) {
                binding.predictResultTxt.text =
                    getString(R.string.predict_win, args.team, args.rank)
                binding.predictResultRound.visibility = View.GONE
            } else {
                when (mode) {
                    PredictResultAdapter.Mode.WIN -> {
                        binding.predictResultTxt.text =
                            getString(R.string.predict_check, args.team, args.rank)
                    }

                    PredictResultAdapter.Mode.ROUND -> {
                        binding.predictResultTxt.text =
                            getString(R.string.predict_check_rnd, args.team, args.rank)
                    }
                }
            }
        }
    }
}