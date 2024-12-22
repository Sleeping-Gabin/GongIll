package com.example.myapplication.view.ui

import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.VERTICAL
import com.example.myapplication.R
import com.example.myapplication.databinding.PredictResultFragmentBinding
import com.example.myapplication.objects.PredictResult
import com.example.myapplication.view.adapter.PredictResultAdapter
import com.google.android.material.color.MaterialColors

class PredictResultFragment: Fragment() {
    private val args: PredictResultFragmentArgs by navArgs()
    private lateinit var binding: PredictResultFragmentBinding

    private lateinit var result: PredictResult
    private var mode= PredictResultAdapter.Mode.WIN
    private lateinit var adapter: PredictResultAdapter

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
        adapter = PredictResultAdapter(result)
        binding.predictResultView.adapter = adapter

        binding.predictResultBtns.addOnButtonCheckedListener { _, checkedId, isChecked ->
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
        if (result.reverse)  //실패 & 라운드 비교
            setPredictTextReverse()
        else //성공 & 라운드 비교
            setPredictTextNormal()
    }

    private fun setPredictTextReverse() {
        binding.predictResultWin.text = "확정 실패"

        if (result.winScenario.isEmpty() && result.roundScenario.isEmpty()) { //확정 성공
            binding.predictResultTxt.text =
                getString(R.string.predict_perfect_win, args.team, args.rank)

            binding.predictResultWin.visibility = View.GONE
            binding.predictResultRound.visibility = View.GONE
        }
        else if (result.winScenario.isEmpty()) { //실패 상황 없음
            binding.predictResultTxt.text =
                getString(
                    R.string.predict_reverse_rnd,
                    result.roundScenario.size,
                    args.team,
                    args.rank
                )

            binding.predictResultWin.visibility = View.GONE
            binding.predictResultBtns.check(R.id.predictResult_round)
        }
        else if (result.roundScenario.isEmpty()) { //라운드 비교 상황 없음
            val text = getString(
                R.string.predict_reverse_win,
                result.winScenario.size,
                args.team,
                args.rank
            )
            val color =
                MaterialColors.getColor(binding.root, com.google.android.material.R.attr.colorError)
            val spannable = SpannableString(text)
            val length = result.winScenario.size.toString().length
            spannable.setSpan(ForegroundColorSpan(color), 10+length, 15+length, 0)
            binding.predictResultTxt.text = spannable

            binding.predictResultRound.visibility = View.GONE
        }
        else {
            when (mode) {
                PredictResultAdapter.Mode.WIN -> {
                    val text = getString(
                        R.string.predict_reverse_check,
                        result.winScenario.size,
                        args.team,
                        args.rank
                    )
                    val color = MaterialColors.getColor(
                        binding.root,
                        com.google.android.material.R.attr.colorError
                    )
                    val spannable = SpannableString(text)
                    spannable.setSpan(
                        ForegroundColorSpan(color),
                        text.length - 29,
                        text.length - 23,
                        0
                    )
                    binding.predictResultTxt.text = spannable
                }
                PredictResultAdapter.Mode.ROUND -> {
                    binding.predictResultTxt.text =
                        getString(
                            R.string.predict_check_rnd,
                            result.roundScenario.size,
                            args.team,
                            args.rank
                        )
                }
            }

        }
    }

    private fun setPredictTextNormal() {
        binding.predictResultWin.text = "확정 성공"

        if (result.winScenario.isEmpty() && result.roundScenario.isEmpty()) { //확정 실패
            val text = getString(R.string.predict_null, args.team, args.rank)
            val color =
                MaterialColors.getColor(binding.root, com.google.android.material.R.attr.colorError)
            val spannable = SpannableString(text)
            spannable.setSpan(ForegroundColorSpan(color), text.length - 5, text.length, 0)
            binding.predictResultTxt.text = spannable

            binding.predictResultWin.visibility = View.GONE
            binding.predictResultRound.visibility = View.GONE
        }
        else if (result.roundScenario.size==1 && result.roundScenario[0].teamResults.isEmpty()) { //모든 상황 라운드 비교 필요
            binding.predictResultTxt.text = getString(R.string.predict_perfect_rnd, args.team, args.rank)

            binding.predictResultWin.visibility = View.GONE

            adapter.clear()
        }
        else if (result.winScenario.isEmpty()) { //성공 상황 없음
            val text =
                getString(R.string.predict_rnd, result.roundScenario.size, args.team, args.rank)
            val color =
                MaterialColors.getColor(binding.root, com.google.android.material.R.attr.colorError)
            val spannable = SpannableString(text)
            spannable.setSpan(ForegroundColorSpan(color), text.length - 7, text.length - 4, 0)
            binding.predictResultTxt.text = spannable

            binding.predictResultWin.visibility = View.GONE
            binding.predictResultBtns.check(R.id.predictResult_round)
        }
        else if (result.roundScenario.isEmpty()) { //라운드 비교 상황 없음
            binding.predictResultTxt.text =
                getString(R.string.predict_win, result.winScenario.size, args.team, args.rank)

            binding.predictResultRound.visibility = View.GONE
        }
        else {
            when (mode) {
                PredictResultAdapter.Mode.WIN -> {
                    binding.predictResultTxt.text =
                        getString(
                            R.string.predict_check,
                            result.winScenario.size,
                            args.team,
                            args.rank
                        )
                }
                PredictResultAdapter.Mode.ROUND -> {
                    binding.predictResultTxt.text =
                        getString(
                            R.string.predict_check_rnd,
                            result.roundScenario.size,
                            args.team,
                            args.rank
                        )
                }
            }
        }
    }
}