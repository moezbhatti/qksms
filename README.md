# QKSMS

[![Join the chat at https://gitter.im/moezbhatti/qksms](https://badges.gitter.im/moezbhatti/qksms.svg)](https://gitter.im/moezbhatti/qksms?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![ZenHub](https://raw.githubusercontent.com/ZenHubIO/support/master/zenhub-badge.png)](https://zenhub.io)
[![Build Status](https://travis-ci.org/moezbhatti/qksms.svg?branch=master)](https://travis-ci.org/moezbhatti/qksms)

QKSMS is an open source replacement to the [stock messaging app](https://github.com/android/platform_packages_apps_mms) on Android. It is currently available on the Google Play Store.

Disclaimer: The current condition of the codebase is quite poor *(I guess that's what happens when you start writing an app when you don't even know how to code yet)*. I'm in the process of rebuilding the app from scratch using Kotlin - I would recommend against making any pull requests until the new version is ready. ETA October 2017

[![Download on Google Play](http://i.imgur.com/rHhHvZw.png)](https://play.google.com/store/apps/details?id=com.moez.QKSMS)
[![Download on F-Droid](https://f-droid.org/wiki/images/0/06/F-Droid-button_get-it-on.png)](https://f-droid.org/repository/browse/?fdid=com.moez.QKSMS)

<img src="http://i.imgur.com/uwWmDv9.png" width="216" height="384" />
<img src="http://i.imgur.com/p7063VN.png" width="216" height="384" />
<img src="http://i.imgur.com/Z8Rqb7A.png" width="216" height="384" />

# Contributing to QKSMS

We highly encourage contributions to QKSMS; we want to make it as easy and streamlined as possible. If you have any suggestions on how we can improve our workflow, please feel free to contact me.

Quite a bit of old code still needs to be cleaned up and refactored, so keep that in mind as you're digging through the source. If you find something that's just really crappy, let me know and I'll put it higher up on my list of stuff to clean up. I'll be focusing on improving code quality of existing code over the next couple weeks.

### Collaboration

We use GitHub issues to keep track of bugs and feature requests, but we communicate using [Slack](https://qklabs.slack.com/). To join the Slack group, send an email to team@qklabs.com with the subject `QK Labs Slack Invite` (no need to enter a body, we'll send an invite to your email address). If you plan on contributing at some point, we highly recommend joining the slack group because it gives you a direct way to communicate with the rest of the contributors.

### Funds

If you'd like to donate to help support the project, you can donate via [PayPal here](http://bit.ly/QKSMSDonation).

### Translations

Please do not perform translations directly on the source files. Pull requests for translations will not be accepted. If you would like to do translations for QKSMS, join the translation project on Crowdin: https://crowdin.com/project/qksms. 

Unfortunately I don't always have time to check notifications on Crowdin. If you'd like something checked or approved, send me a message on Slack and then I'll take a look

### Development

We encourage you to make pull requests. Whenever you make one, we'll review the code and test it to make sure that it fits the code style guidelines, and achieves what the pull request intends. We'll discuss any problems or potential improvements, and we'll merge it when we're both (or any number of willing participants) happy with the code.

If you're new, we recommend that you look through the issues and try to fix some of the more simple sounding bugs to familiarize yourself with the codebase.

If you'd like to work on a new feature, please create an issue so we can discuss the idea together before implementation begins.

To learn about pull requests, please refer to the following guide: https://help.github.com/articles/using-pull-requests/.

### Code style

We follow the code style guidelines set by Google for contributors to AOSP: https://source.android.com/source/code-style.html

The current codebase is far from being 100% at this standard, but updating existing code to follow the unified guidelines can be a good way to introduce yourself to the codebase and start getting familiar with it.

Any new code written should follow these guidelines.

# Bug Reports / Feature Requests

We track issues and feature requests using GitHub issues. You can view existing issues and create your own here: https://github.com/moezbhatti/qksms/issues

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

You can see bugs which need "repro steps" [here](https://github.com/moezbhatti/qksms/issues?q=is%3Aopen+is%3Aissue+label%3Aneeds-repro). If you see any bugs that you know how to reproduce (see above for example repro steps), then make a comment with the steps and we'll track the bug down!

#### Making enhancement requests
There are no rules for enhancement requests. Your ideas are all awesome. Although we may not implement every suggestion, we love brainstorming with you and hearing your feedback.

To make an enhancement request, add an "issue".

# Contact

QKSMS is developed and maintained by [Moez Bhatti](https://github.com/moezbhatti). Feel free to reach out to moez@qklabs.com

# License

QKSMS is released under the **The GNU General Public License v3.0 (GPLv3)**, which can be found in the `LICENSE` file in the root of this project.
