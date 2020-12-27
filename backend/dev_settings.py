"""
Django settings for api project.

Generated by 'django-admin startproject' using Django 2.0.3.

For more information on this file, see
https://docs.djangoproject.com/en/2.0/topics/settings/

For the full list of settings and their values, see
https://docs.djangoproject.com/en/2.0/ref/settings/
"""

import os
from datetime import timedelta
from settings import *

# Import sensitive settings.
try:
    from dev_settings_sensitive import *
except ImportError:
    print("Error: dev_settings_sensitive.py not found.")
    print("Some variables in this file will not be defined properly.")
    # Set some default values, in case the sensitive settings hadn't been defined.
    DB_PASS = 'redacted'
    DB_HOST = 'redacted'
    ADMIN_PASS='redacted'
    SENDGRID_API_KEY='redacted'
    GOOGLE_APPLICATION_CREDENTIALS='redacted'

# Build paths inside the project like this: os.path.join(BASE_DIR, ...)
BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))


# Quick-start development settings - unsuitable for production
# See https://docs.djangoproject.com/en/2.0/howto/deployment/checklist/

# SECURITY WARNING: keep the secret key used in production secret!
# SECRET_KEY = "redacted"

# SECURITY WARNING: don't run with debug turned on in production!
# DEBUG = True
DEBUG = False
ADMINS = [('nathan', 'n8kim1@gmail.com')]
LOGGING = {
    'version': 1,
    'disable_existing_loggers': False,
    'handlers': {
        'mail_admins': {
            'level': 'ERROR',
            'class': 'django.utils.log.AdminEmailHandler',
            'include_html': True,
        },
        'console': {
            'class': 'logging.StreamHandler',
        }
    },
    'loggers': {
        'django': {
            'handlers': ['console'],
            'level': 'INFO',
        },
        'django.request': {
            'handlers': ['mail_admins'],
            'level': 'ERROR',
            'propagate': True,
        },
    },
}

# Application definition
INSTALLED_APPS += ['debug_toolbar']

MIDDLEWARE += ['debug_toolbar.middleware.DebugToolbarMiddleware']

# Database
# https://docs.djangoproject.com/en/2.0/ref/settings/#databases

DATABASES = {
    'default': {
        'ENGINE': 'django.db.backends.postgresql_psycopg2',
        'NAME': 'battlecode',
        'USER': 'battlecode',
        'PASSWORD': DB_PASS,
        'HOST': DB_HOST,
        'PORT': 5432,
    }
}
