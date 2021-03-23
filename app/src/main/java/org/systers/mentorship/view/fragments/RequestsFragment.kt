package org.systers.mentorship.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import org.systers.mentorship.R
import org.systers.mentorship.databinding.FragmentRequestsBinding
import org.systers.mentorship.view.activities.MainActivity
import org.systers.mentorship.view.adapters.RequestsPagerAdapter
import org.systers.mentorship.viewmodels.RequestsViewModel

/**
 * The fragment is responsible for showing the all mentorship requests
 * and filtered lists such as for pending requests and past relations and requests
 */
class RequestsFragment : BaseFragment() {

    private var _requestsBinding: FragmentRequestsBinding? = null
    private val requestsBinding get() = _requestsBinding!!

    companion object {
        /**
         * Creates an instance of [RequestsFragment]
         */
        fun newInstance() = RequestsFragment()

        val TAG = RelationFragment::class.java.simpleName
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _requestsBinding = FragmentRequestsBinding.inflate(inflater, container, false)
        return requestsBinding.root
    }

    private val requestsViewModel by lazy {
        ViewModelProviders.of(this).get(RequestsViewModel::class.java)
    }
    private val activityCast by lazy { activity as MainActivity }

    override fun getLayoutResourceId(): Int = R.layout.fragment_requests

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setHasOptionsMenu(true)
        requestsBinding.srlRequests.setOnRefreshListener { fetchNewest() }

        requestsViewModel.successful.observe(this, Observer { successful ->
            requestsBinding.srlRequests.isRefreshing = false
            if (successful != null) {
                if (successful) {
                    requestsViewModel.pendingSuccessful.observe(this, Observer { successful ->
                        activityCast.hideProgressDialog()
                        if (successful != null) {
                            if (successful) {
                                requestsViewModel.allRequestsList?.let { allRequestsList ->
                                    requestsBinding.apply {
                                        vpMentorshipRequests.adapter = RequestsPagerAdapter(allRequestsList, requestsViewModel.pendingAllRequestsList, requireActivity())
//                                    tlMentorshipRequests.setupWithViewPager(vpMentorshipRequests)
                                        TabLayoutMediator(tlMentorshipRequests, vpMentorshipRequests) { tab, position ->
                                            when (position) {
                                                0 -> {
                                                    tab.text = context?.getString(R.string.pending)
                                                }
                                                1 -> {
                                                    tab.text = context?.getString(R.string.past)
                                                }
                                                2 -> {
                                                    tab.text = context?.getString(R.string.all)
                                                }
                                            }
                                        }.attach()
                                    }
                                }
                            }
                        }
                    })
                } else {
                    view?.let {
                        requestsViewModel.message?.let { message ->
                            Snackbar.make(it, message, Snackbar.LENGTH_LONG).show()
                        }
                    }
                }
            }
        })

        fetchNewest()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_refresh -> {
                fetchNewest()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun fetchNewest() {
        requestsBinding.srlRequests.isRefreshing = true
        requestsViewModel.getAllMentorshipRelations()
        requestsViewModel.getAllPendingMentorshipRelations()
        requestsViewModel.getPastMentorshipRelations()
    }
}
