# QKSms Documentation
This folder serves as an initial documentation and GH-Pages site for QKSMS by moezbhatti.

All documentation *should* be written in [Markdown](https://guides.github.com/features/mastering-markdown/)

## Usage
We use ![MkDocs](docs/assets/images/icon.ico)[MkDocs](https://www.mkdocs.org) for maintaining and building these docs.

### Installation
- Clone this repo
    ```console
    git clone git@github.com:moezbhatti/qksms
    cd docs
    git submodule update --recursive
    ```
- [Install MkDocs](https://www.mkdocs.org/#installation) and associated plugins
    ```console
    pip install -r requirements.txt
    ```

### Preview
- Use `mkdocs serve` to preview the site locally
    ```console
    $ mkdocs serve
    INFO    -  Building documentation...
    INFO    -  Cleaning site directory
    [I 180115 12:36:57 server:283] Serving on http://127.0.0.1:8000
    [I 180115 12:36:57 handlers:60] Start watching changes
    [I 180115 12:36:57 handlers:62] Start detecting changes
    ```

### Deploying
- Deploy to Github:
    ```console
    $ mkdocs gh-deploy
    INFO    -  Cleaning site directory·
    INFO    -  Building documentation to directory: /home/user/Git/moezbhatti/qksms/docs/site·
    INFO    -  Copying '/home/user/Git/moezbhatti/qksms/docs/site' to 'gh-pages' branch and pushing to GitHub.·
    INFO    -  Your documentation should shortly be available at: https://moezbhatti.github.io/qksms/·
    ```

## Contributing
WIP

## FAQ
WIP

## Credits
WIP
