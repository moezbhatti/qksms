# QKSMS

QKSMS is an open source replacement to the [stock messaging app](https://github.com/android/platform_packages_apps_mms) on Android. It is currently available on the Google Play Store

[![Download](http://developer.android.com/images/brand/en_generic_rgb_wo_60.png)](https://play.google.com/store/apps/details?id=com.moez.QKSMS)

<img src="https://lh3.googleusercontent.com/H5cGsAZKt6wscgWnFMpRZ-JvKt1Yt7uICuSFKV4E4oSdpusPkTfYZHELDj5aqnmCuw=h900-rw" width="216" height="384" />
<img src="https://lh3.googleusercontent.com/KAdJyicmKspdNblU_Js1GLQat0q5PaXY_oOKma0V_ycIaW4XW915t9sZ_BkpsacgPG8=h900-rw" width="216" height="384" />
<img src="https://lh3.googleusercontent.com/24Y9Ookd4A2e6y4-sHkiROXf2NzIppRdjT4Fnusl91p9_Z8aUyLfGmqyb5DLHyavyf0=h900-rw" width="216" height="384" />

# Contributing to QKSMS

We highly encourage contributions to QKSMS; we want to make it as easy and streamlined as possible. If you have any suggestions on how we can improve our workflow, please feel free to contact me

Quite a bit of old code still needs to be cleaned up and refactored, so keep that in mind as you're digging through the source. If you find something that's just really crappy, let me know and I'll put it higher up on my list of stuff to clean up. I'll be focusing on improving code quality of existing code over the next couple weeks

### Collaboration

We use Github issues to keep track of bugs and feature requests, but we communicate using [Slack](https://qklabs.slack.com/). To join the Slack group, send an email to team@qklabs.com with the subject `QK Labs Slack Invite` (no need to enter a body, we'll send an invite to your email address). If you plan on contributing at some point, we highly recommend joining the slack group because it gives you a direct way to communicate with the rest of the contributors

### Funds

If you'd lke to donate to help support the project, you can donate via [PayPal here](http://bit.ly/QKSMSDonation)

### Development

We encourage you to make pull requests. Whenever you make one, we'll review the code and test it to make sure that it fits the code style guidelines, and achieves what the pull request intends. We'll discuss any problems or potention improvements, and we'll merge it when we're both (or any number of willing participants) happy with the code

If you're new, we recommend that you look through the issues and try to fix some of the more simple sounding bugs to familiarize yourself with the codebase

If you'd like to work on a new feature, please create an issue so we can discuss the idea together before implementation begins.

To learn about pull requests, please refer to the following guide: https://help.github.com/articles/using-pull-requests/

When creating a pull request, only merge your branch into the `develop` branch. Never attempt to merge directly into `master`. The `master` branch will be used only for stable, production ready releases.

### Code style

We follow the code style guidelines set by Google for contributors to AOSP: https://source.android.com/source/code-style.html

The current codebase is far from being 100% at this standard, but updating existing code to follow the unified guidelines can be a good way to introduce yourself to the codebase and start getting familiar with it.

Any new code written should follow these guidelines

# Bug Reports / Feature Requests

We track issues and pull requests using GitHub issues. You can view existing issues and create your own here: https://github.com/qklabs/qksms/issues

### How to help

1. Report bugs
2. Help reproduce bugs
3. Make enhancement requests (it would be great if QKSMS...)

#### Reporting bugs
A great bug report contains a description of the problem and steps to reproduce the problem. We need to know what we're looking for and where to look for it. If the QKSMS team can't reproduce a bug, we will work with testers to fix the problem.

Bugs are given the red `bug` label. Bugs also may be given extra orange labels:

`needs-info`: We need a better description of the problem.  
`needs-repro`: We need someone who can reproduce the issue consistently.  
`needs-review`: We need someone to verify that the bug has been squashed.

##### Example bug report submission
> *This is a real bug as of the time of writing. Try reproducing it!*

```
### DESCRIPTION
Conversations aren't marked as read when they are opened from a notification.

### STEPS
1. Long press on a conversation and press "Mark as unread" to reveal a notification.
2. Press the home button to put QKSMS in the background.
3. Pull down on the notification to open the notification drawer.
4. Tap on the body of the notification (not the buttons).
5. Tap on the back button to reveal the conversation list.

### EXPECTED
The conversation is marked as read.

### OBSERVATIONS
The conversation isn't marked as read.
```

#### Helping reproduce bugs
Reproducing a bug is important, since if we can't reproduce an issue then it's hard to figure out what's going wrong, and we can't know that we've fixed it. 

To see bugs that need "repro steps", go to the Issues section and search for `is:open label:needs-repro`. If you see any bugs that you know how to reproduce (see above for example repro steps), then make a comment with the steps and we'll track the bug down!

#### Making enhancement requests
There are no rules for enhancement requests. Your ideas are all awesome. Although we may not implement every suggestion, we love brainstorming with you and hearing your feedback.

To make an enhancement request, add an "issue" with the "enhancement" label.

# License

QKSMS is developed and maintained by [Moez Bhatti](https://github.com/moezbhatti) and released under the **The GNU General Public License v3.0 (GPLv3)**, which can be found in the `LICENSE` file in the root of this project
