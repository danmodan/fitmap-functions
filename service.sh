#!/bin/bash

BASEDIR="$( cd "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P )"

run_maven() {

    docker run --rm \
               -w /build \
               -v $BASEDIR:/build \
               -v ~/.m2:/root/.m2 \
               maven:3-adoptopenjdk-11-openj9 $@
}

deploy_http_function() {

    gcloud functions deploy $1 \
                            --region=southamerica-east1 \
                            --entry-point $2 \
                            --runtime java11 \
                            --trigger-http \
                            --max-instances 800 \
                            --security-level=secure-always \
                            --set-env-vars=GOOGLE_CLOUD_PROJECT=$GOOGLE_CLOUD_PROJECT
}

case $1 in
    check-deploy)
        gcloud meta list-files-for-upload
        ;;
    deploy-subscription-plan-func-v2)
        deploy_http_function subscription-plan-func-v2 com.fitmap.function.v2.SubscriptionPlanFunction
        ;;
    deploy-student-func-v2)
        deploy_http_function student-func-v2 com.fitmap.function.v2.StudentFunction
        ;;
    deploy-sport-func-v2)
        deploy_http_function sport-func-v2 com.fitmap.function.v2.SportFunction
        ;;
    deploy-set-roles-func-v2)
        deploy_http_function set-roles-func-v2 com.fitmap.function.v2.SetRolesFunction
        ;;
    deploy-send-account-management-email-func-v2)
        gcloud functions deploy send-account-management-email-func-v2 \
            --region=southamerica-east1 \
            --entry-point com.fitmap.function.v2.SendAccountManagementEmailFunction \
            --runtime java11 \
            --trigger-http \
            --max-instances 800 \
            --security-level=secure-always \
            --set-env-vars=GOOGLE_CLOUD_PROJECT=$GOOGLE_CLOUD_PROJECT,SENDGRID_API_KEY=$SENDGRID_API_KEY,EMAIL_VERIFY_TEMPLATE_ID=$EMAIL_VERIFY_TEMPLATE_ID,RESET_PASSWORD_TEMPLATE_ID=$RESET_PASSWORD_TEMPLATE_ID,SENDGRID_FROM_NAME=$SENDGRID_FROM_NAME,SENDGRID_FROM_EMAIL=$SENDGRID_FROM_EMAIL,ACCOUNT_ALERTS_UNSUBSCRIBE_GROUP_ID=$ACCOUNT_ALERTS_UNSUBSCRIBE_GROUP_ID
        ;;
    deploy-personal-trainer-func-v2)
        deploy_http_function personal-trainer-func-v2 com.fitmap.function.v2.PersonalTrainerFunction
        ;;
    deploy-locations-func-v2)
        deploy_http_function locations-func-v2 com.fitmap.function.v2.LocationsFunction
        ;;
    deploy-gym-func-v2)
        deploy_http_function gym-func-v2 com.fitmap.function.v2.GymFunction
        ;;
    deploy-focus-func-v2)
        deploy_http_function focus-func-v2 com.fitmap.function.v2.FocusFunction
        ;;
    deploy-fight-func-v2)
        deploy_http_function fight-func-v2 com.fitmap.function.v2.FightFunction
        ;;
    deploy-event-func-v2)
        deploy_http_function event-func-v2 com.fitmap.function.v2.EventFunction
        ;;
    *)
        echo -e "Invalid option"
        ;;
esac
