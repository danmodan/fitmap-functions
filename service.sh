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
                            --max-instances 80 \
                            --set-env-vars=GOOGLE_CLOUD_PROJECT=$GOOGLE_CLOUD_PROJECT
}

case $1 in
    check-deploy)
        gcloud meta list-files-for-upload
        ;;
    deploy-v1-set-roles)
        deploy_http_function v1-set-roles com.fitmap.function.setroles.v1.SetRolesFunction
        ;;
    deploy-v1-gym-crud-gym)
        deploy_http_function v1-gym-crud-gym com.fitmap.function.gymcontext.v1.CrudGymFunction
        ;;
    deploy-v1-gym-crud-contacts)
        deploy_http_function v1-gym-crud-contacts com.fitmap.function.gymcontext.v1.CrudContactFunction
        ;;
    *)
        echo -e "Invalid option"
        ;;
esac
