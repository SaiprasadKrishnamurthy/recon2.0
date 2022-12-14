from os.path import exists

from util.common_login import common_login


def get_credentials():
    token = ''
    if exists(".token.txt"):
        with open('.token.txt', 'r') as file:
            token = str(file.read()).strip().replace('\n', '')
    else:
        common_login()
        if exists(".token.txt"):
            with open('.token.txt', 'r') as file:
                token = str(file.read()).strip().replace('\n', '')
    return token
