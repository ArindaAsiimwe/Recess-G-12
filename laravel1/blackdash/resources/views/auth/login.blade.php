@extends('layouts.app')

@section('content')
    {{-- <div class="col-md-12 text-center ml-auto mr-auto" style="background-color: rgb(55, 160, 14)">
        <h3 class="mb-5">Login to enjoy our services!</h3>
    </div> --}}

    <!-- Content Header (Page header) -->
    <div class="content-header">
        <div class="container-fluid">
          <div class="row mb-2">
            <div class="col-sm-6">
             
            </div><!-- /.col -->
            <div class="col-sm-6">
              
            </div><!-- /.col -->
          </div><!-- /.row -->
        </div><!-- /.container-fluid -->
      </div>
      <!-- /.content-header -->


    <div class="col-lg-4 col-md-6 ml-auto mr-auto" style="position: absolute">
        <form class="form" method="post" action="{{ route('login') }}">
            @csrf

            <div class="card card-login card-white" >
                <div class="card-header">
                    {{-- <img src="{{ asset('black') }}/img/card-primary.png" alt=""> --}}
                    <h1 class="card-title">{{ __('Log in') }}</h1>
                </div>
                <div class="card-body" ">
                    {{--   --}}
                    <div class="input-group{{ $errors->has('email') ? ' has-danger' : '' }}">
                        <div class="input-group-prepend">
                            <div class="input-group-text">
                                <i class="tim-icons icon-email-85"></i>
                            </div>
                        </div>
                        <input type="email" name="email" class="form-control{{ $errors->has('email') ? ' is-invalid' : '' }}" placeholder="{{ __('Email') }}">
                        @include('alerts.feedback', ['field' => 'email'])
                    </div>
                    <div class="input-group{{ $errors->has('password') ? ' has-danger' : '' }}">
                        <div class="input-group-prepend">
                            <div class="input-group-text">
                                <i class="tim-icons icon-lock-circle"></i>
                            </div>
                        </div>
                        <input type="password" placeholder="{{ __('Password') }}" name="password" class="form-control{{ $errors->has('password') ? ' is-invalid' : '' }}">
                        @include('alerts.feedback', ['field' => 'password'])
                    </div>
                </div>
                <div class="card-footer">
                    <button type="submit" href="" class="btn btn-primary btn-lg btn-block mb-3">{{ __('Login') }}</button>
                    <div class="pull-left">
                        <h6>
                            <a href="{{ route('register') }}" class="link footer-link">{{ __('Dont have an account? Sign up') }}</a>
                        </h6>
                    </div>
                    <div class="pull-right">
                        <h6>
                            <a href="{{ route('password.request') }}" class="link footer-link">{{ __('Forgot password?') }}</a>
                        </h6>
                    </div>
                </div>
            </div>
        </form>
    </div>
@endsection
